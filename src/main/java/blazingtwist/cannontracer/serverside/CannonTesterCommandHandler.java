package blazingtwist.cannontracer.serverside;

import blazingtwist.cannontracer.shared.datatypes.FinalVec3d;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import blazingtwist.cannontracer.shared.utils.TextUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.CommandBlockExecutor;

public class CannonTesterCommandHandler {

	public static CannonTesterCommandHandler getInstance() {
		if (instance == null) {
			instance = new CannonTesterCommandHandler();
		}
		return instance;
	}

	public static boolean isCannonTesterCommand(String cmd) {
		return cmd != null && cmd.startsWith(commandPrefix);
	}

	public static boolean isCommandBlock(BlockState block) {
		return block.isOf(Blocks.COMMAND_BLOCK)
				|| block.isOf(Blocks.CHAIN_COMMAND_BLOCK)
				|| block.isOf(Blocks.REPEATING_COMMAND_BLOCK);
	}

	public static final String commandPrefix = "[TestCannonData]";

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES,
					MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS,
					MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
			.disable(MapperFeature.AUTO_DETECT_CREATORS,
					MapperFeature.AUTO_DETECT_FIELDS,
					MapperFeature.AUTO_DETECT_GETTERS,
					MapperFeature.AUTO_DETECT_IS_GETTERS,
					MapperFeature.AUTO_DETECT_SETTERS)
			.build();

	private static CannonTesterCommandHandler instance;

	private final List<ScheduledTick> scheduledTicks = new ArrayList<>();

	private CannonTesterCommandHandler() {
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	public TestCannonData parseTestCannon(String data, ServerWorld world, Vec3d blockPos) {
		if (data == null || data.isBlank()) {
			return new TestCannonData();
		}

		try {
			return OBJECT_MAPPER.readValue(data, TestCannonData.class);
		} catch (JsonProcessingException e) {
			MutableText message = new TextUtils().formatted(Formatting.RED).text("TextCannon-Block at location ")
					.formatted(Formatting.DARK_PURPLE).text(blockPos.toString())
					.formatted(Formatting.RED).text(" failed! (invalid content) Error: ")
					.formatted(Formatting.DARK_PURPLE).text(e.getMessage())
					.build();
			for (ServerPlayerEntity player : world.getPlayers()) {
				player.sendMessage(message);
			}
			e.printStackTrace();
			return null;
		}
	}

	public void writeCannonToBlock(ServerPlayerEntity player, Vec3i cmdPos, TestCannonData cannon) {

		BlockPos blockPos = new BlockPos(cmdPos);
		BlockState blockState = player.world.getBlockState(blockPos);
		if (blockState == null || blockState.isAir() || !isCommandBlock(blockState)) {
			player.sendMessage(Text.literal("Targeted Command-Block does not exist anymore!").formatted(Formatting.RED));
			return;
		}

		BlockEntity blockEntity = player.world.getBlockEntity(blockPos);
		if (!(blockEntity instanceof CommandBlockBlockEntity commandEntity)) {
			player.sendMessage(Text.literal("Targeted Command-Block does not exist anymore!").formatted(Formatting.RED));
			return;
		}

		String json;
		try {
			json = OBJECT_MAPPER.writeValueAsString(cannon);
		} catch (JsonProcessingException e) {
			player.sendMessage(new TextUtils()
					.formatted(Formatting.RED).text("Unable to store cannon in Command-Block due to exception: ")
					.formatted(Formatting.DARK_PURPLE).text(e.getMessage())
					.build());
			e.printStackTrace();
			return;
		}
		commandEntity.getCommandExecutor().setCommand(commandPrefix + json);
	}

	public void execute(CommandBlockExecutor executor, String cmd) {
		cmd = cmd.substring(commandPrefix.length());
		Vec3d blockLocation = executor.getPos();
		ServerWorld world = executor.getWorld();

		TestCannonData testCannon = parseTestCannon(cmd, world, blockLocation);
		if (testCannon == null) {
			return;
		}

		Vec3d velocity = testCannon.getVelocity().toVec3d();
		FinalVec3d blockOffset = testCannon.getBlockOffset();
		FinalVec3d pixelOffset = testCannon.getPixelOffset();
		double spawnX = blockLocation.x + blockOffset.x() + (pixelOffset.x() / 16d) + testCannon.getXAlign().getBlockOffset();
		double spawnY = blockLocation.y + blockOffset.y() + (pixelOffset.y() / 16d) + testCannon.getYAlign().getBlockOffset() - 0.49d;
		double spawnZ = blockLocation.z + blockOffset.z() + (pixelOffset.z() / 16d) + testCannon.getZAlign().getBlockOffset();

		synchronized (scheduledTicks) {
			for (TestCannonData.CannonCharge charge : testCannon.getCharges()) {
				if (!charge.isEnabled()) {
					continue;
				}
				final int amount = charge.getAmount();
				final boolean random = charge.getRandom();
				scheduledTicks.add(new ScheduledTick(charge.getDelay(), () -> spawnTnt(world, spawnX, spawnY, spawnZ, velocity, amount, random)));
			}
		}
	}

	private void onServerTick(MinecraftServer server) {
		synchronized (scheduledTicks) {
			for (Iterator<ScheduledTick> tickIterator = scheduledTicks.iterator(); tickIterator.hasNext(); ) {
				ScheduledTick tick = tickIterator.next();
				int remainingTicks = tick.ticksRemaining.getAndDecrement();
				if (remainingTicks > 0) {
					continue;
				}

				tick.callback.run();
				tickIterator.remove();
			}
		}
	}

	private void spawnTnt(ServerWorld world, double x, double y, double z, Vec3d velocity, int amount, boolean randomHop) {
		for (int i = 0; i < amount; i++) {
			TntEntity tntEntity = new TntEntity(world, x, y, z, null);
			tntEntity.setFuse(80);
			if (randomHop) {
				tntEntity.setVelocity(tntEntity.getVelocity().add(velocity));
			} else {
				tntEntity.setVelocity(velocity);
			}
			world.spawnEntity(tntEntity);
		}
	}

	private static class ScheduledTick {
		public final AtomicInteger ticksRemaining;
		public final Runnable callback;

		public ScheduledTick(int ticksRemaining, Runnable callback) {
			this.ticksRemaining = new AtomicInteger(ticksRemaining);
			this.callback = callback;
		}
	}
}
