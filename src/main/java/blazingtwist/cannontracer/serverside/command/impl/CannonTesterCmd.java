package blazingtwist.cannontracer.serverside.command.impl;

import blazingtwist.cannontracer.networking.ServerPacketHandler;
import blazingtwist.cannontracer.serverside.CannonTesterCommandHandler;
import blazingtwist.cannontracer.serverside.command.ITracerCommand;
import blazingtwist.cannontracer.serverside.command.SubCommandRegistrar;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import blazingtwist.cannontracer.shared.utils.PlayerUtils;
import blazingtwist.cannontracer.shared.utils.TextUtils;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public enum CannonTesterCmd implements SubCommandRegistrar {
	INSTANCE;

	private static final List<ITracerCommand> subCommands = List.of(
			TesterEditCmd.INSTANCE
	);

	@Override
	public String getCmdPrefix() {
		return "tester";
	}

	@Override
	public void help(TextUtils builder) {
		ITracerCommand.appendCommandDescriptor(this, builder);
		builder.formatted(Formatting.WHITE).text(" - quickly create cannon prototypes.");
	}

	@Override
	public Iterable<ITracerCommand> getSubCommands() {
		return subCommands;
	}

	private enum TesterEditCmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "edit";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - edit the looked at command block.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			BlockPos targetPos = PlayerUtils.getLookedAtBlockPos(player, 10);
			BlockState targetBlock = PlayerUtils.getBlockState(player, targetPos);
			if (targetBlock == null || !CannonTesterCommandHandler.isCommandBlock(targetBlock)) {
				player.sendMessage(Text.literal("You have to look at a command-block to edit a testCannon.").formatted(Formatting.RED));
				return -1;
			}

			BlockEntity blockEntity = player.world.getBlockEntity(targetPos);
			if (!(blockEntity instanceof CommandBlockBlockEntity commandEntity)) {
				player.sendMessage(Text.literal("Failed to read data from command-block.").formatted(Formatting.RED));
				return -1;
			}

			String command = commandEntity.getCommandExecutor().getCommand();
			final TestCannonData cannonData;
			if (command.isBlank()) {
				cannonData = new TestCannonData();
			} else {
				if (!command.startsWith(CannonTesterCommandHandler.commandPrefix)) {
					player.sendMessage(Text.literal("This command-block is not a testCannon, use a different one or clear its content."));
					return -1;
				}
				cannonData = CannonTesterCommandHandler.getInstance().parseTestCannon(
						command.substring(CannonTesterCommandHandler.commandPrefix.length()),
						commandEntity.getCommandExecutor().getWorld(),
						targetPos.toCenterPos()
				);
			}

			ServerPacketHandler.getInstance().serverToClient_sendCannonData(player, targetPos, cannonData);
			return 1;
		}
	}
}
