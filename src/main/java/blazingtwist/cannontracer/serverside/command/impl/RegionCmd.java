package blazingtwist.cannontracer.serverside.command.impl;

import blazingtwist.cannontracer.networking.ServerPacketHandler;
import blazingtwist.cannontracer.serverside.RegionManager;
import blazingtwist.cannontracer.serverside.command.ITracerArgCommand;
import blazingtwist.cannontracer.serverside.command.ITracerCommand;
import blazingtwist.cannontracer.serverside.command.SubCommandRegistrar;
import blazingtwist.cannontracer.serverside.datatype.PlayerSelection;
import blazingtwist.cannontracer.serverside.datatype.Region;
import blazingtwist.cannontracer.shared.utils.BoxUtils;
import blazingtwist.cannontracer.shared.utils.TextUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public enum RegionCmd implements SubCommandRegistrar {
	INSTANCE;

	private static final List<ITracerCommand> subCommands = List.of(
			Pos1Cmd.INSTANCE,
			Pos2Cmd.INSTANCE,
			CreateCmd.INSTANCE,
			DrawCmd.INSTANCE,
			DeleteCmd.INSTANCE,
			ListCmd.INSTANCE,
			TntCmd.INSTANCE
	);

	@Override
	public String getCmdPrefix() {
		return "region";
	}

	@Override
	public void help(TextUtils builder) {
		ITracerCommand.appendCommandDescriptor(this, builder);
		builder.formatted(Formatting.WHITE).text(" - manage regions.");
	}

	@Override
	public Iterable<ITracerCommand> getSubCommands() {
		return subCommands;
	}

	private enum Pos1Cmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "pos1";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - mark the first position for a region.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			BlockPos pos = player.getBlockPos();
			RegionManager.getInstance().setPos1(player, pos);
			MutableText message = new TextUtils()
					.formatted(Formatting.GREEN).text("Set pos1 to ")
					.formatted(Formatting.AQUA).text(pos.toShortString())
					.build();
			player.sendMessage(message);
			return 1;
		}
	}

	private enum Pos2Cmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "pos2";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - mark the second position for a region.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			BlockPos pos = player.getBlockPos();
			RegionManager.getInstance().setPos2(player, pos);
			MutableText message = new TextUtils()
					.formatted(Formatting.GREEN).text("Set pos2 to ")
					.formatted(Formatting.AQUA).text(pos.toShortString())
					.build();
			player.sendMessage(message);
			return 1;
		}
	}

	private enum DrawCmd implements ITracerArgCommand {
		INSTANCE;
		private static final List<ArgDescriptor> ARG_DESCRIPTORS = List.of(
				new ArgDescriptor("name", StringArgumentType.string(), true)
		);

		@Override
		public String getCmdPrefix() {
			return "draw";
		}

		@Override
		public List<ArgDescriptor> getArguments() {
			return ARG_DESCRIPTORS;
		}

		@Override
		public void help(TextUtils builder) {
			ITracerArgCommand.appendCommandDescriptor(this, builder);
			String argName = ARG_DESCRIPTORS.get(0).name();
			builder.formatted(Formatting.WHITE).text(" - draw the bounding box of a region, or clear all drawn regions if '" + argName + "' is omitted.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			ServerPacketHandler.getInstance().serverToClient_clearRegionData(player);
			player.sendMessage(Text.literal("Cleared region boxes.").formatted(Formatting.GREEN));
			return 1;
		}

		@Override
		public int onExecute(ServerPlayerEntity player, Object[] args) {
			String argName = (String) args[0];

			Region region = RegionManager.getInstance().getRegion(argName);
			if (region == null) {
				player.sendMessage(new TextUtils()
						.formatted(Formatting.RED).text("Region ")
						.formatted(Formatting.DARK_PURPLE).text(argName)
						.formatted(Formatting.RED).text(" does not exist.")
						.build());
				return -1;
			}

			ServerPacketHandler.getInstance().serverToClient_sendRegionData(player, region);
			player.sendMessage(new TextUtils()
					.formatted(Formatting.GREEN).text("Loaded region ")
					.formatted(Formatting.AQUA).text(argName)
					.build());
			return 1;
		}
	}

	private enum CreateCmd implements ITracerArgCommand {
		INSTANCE;
		private static final List<ArgDescriptor> ARG_DESCRIPTORS = List.of(
				new ArgDescriptor("name", StringArgumentType.string(), false)
		);

		@Override
		public String getCmdPrefix() {
			return "create";
		}

		@Override
		public List<ArgDescriptor> getArguments() {
			return ARG_DESCRIPTORS;
		}

		@Override
		public void help(TextUtils builder) {
			ITracerArgCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - create or update a region.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			player.sendMessage(Text.literal("You must provide a region name.").formatted(Formatting.RED));
			return -1;
		}

		@Override
		public int onExecute(ServerPlayerEntity player, Object[] args) {
			RegionManager regionManager = RegionManager.getInstance();
			PlayerSelection playerSelection = regionManager.getPlayerSelection(player);
			if (playerSelection == null || playerSelection.getPos1() == null || playerSelection.getPos2() == null) {
				player.sendMessage(Text.literal("You must select a pos1 and pos2 before creating a region.").formatted(Formatting.RED));
				return -1;
			}

			String regionName = (String) args[0];
			Region createdRegion = regionManager.createRegion(regionName, playerSelection);
			player.sendMessage(new TextUtils()
					.formatted(Formatting.GREEN).text("Region ")
					.formatted(Formatting.AQUA).text(regionName)
					.formatted(Formatting.GREEN).text(" now covers bounding box ")
					.formatted(Formatting.AQUA).text(createdRegion.getBoundingBox().toString())
					.build());
			return 1;
		}
	}

	private enum DeleteCmd implements ITracerArgCommand {
		INSTANCE;
		private static final List<ArgDescriptor> ARG_DESCRIPTORS = List.of(
				new ArgDescriptor("name", StringArgumentType.string(), false)
		);

		@Override
		public String getCmdPrefix() {
			return "delete";
		}

		@Override
		public List<ArgDescriptor> getArguments() {
			return ARG_DESCRIPTORS;
		}

		@Override
		public void help(TextUtils builder) {
			ITracerArgCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - delete an existing region.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			player.sendMessage(Text.literal("You must provide a region name.").formatted(Formatting.RED));
			return -1;
		}

		@Override
		public int onExecute(ServerPlayerEntity player, Object[] args) {
			RegionManager regionManager = RegionManager.getInstance();
			String regionName = (String) args[0];

			boolean didExist = regionManager.deleteRegion(regionName);
			if (!didExist) {
				player.sendMessage(new TextUtils()
						.formatted(Formatting.RED).text("Region ")
						.formatted(Formatting.DARK_PURPLE).text(regionName)
						.formatted(Formatting.RED).text(" does not exist.")
						.build());
				return -1;
			}

			player.sendMessage(new TextUtils()
					.formatted(Formatting.GREEN).text("Region ")
					.formatted(Formatting.AQUA).text(regionName)
					.formatted(Formatting.GREEN).text(" successfully was deleted.")
					.build());
			return 1;
		}
	}

	private enum ListCmd implements ITracerArgCommand {
		INSTANCE;
		private static final List<ArgDescriptor> ARG_DESCRIPTORS = List.of(
				new ArgDescriptor("range", IntegerArgumentType.integer(), true)
		);

		@Override
		public String getCmdPrefix() {
			return "list";
		}

		@Override
		public List<ArgDescriptor> getArguments() {
			return ARG_DESCRIPTORS;
		}

		@Override
		public void help(TextUtils builder) {
			ITracerArgCommand.appendCommandDescriptor(this, builder);
			String rangeArgName = ARG_DESCRIPTORS.get(0).name();
			builder.formatted(Formatting.WHITE).text(" - list all regions, optionally within '" + rangeArgName + "' blocks of distance.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			List<String> regions = RegionManager.getInstance().getRegionNames();
			if (regions.isEmpty()) {
				player.sendMessage(Text.literal("There are no regions yet.").formatted(Formatting.GREEN));
				return 1;
			}

			player.sendMessage(new TextUtils()
					.formatted(Formatting.GREEN).text("Found ")
					.formatted(Formatting.AQUA).text(Integer.toString(regions.size()))
					.formatted(Formatting.GREEN).text(" regions. ")
					.list(", ", regions, Formatting.AQUA)
					.build());
			return 1;
		}

		@Override
		public int onExecute(ServerPlayerEntity player, Object[] args) {
			int range = (Integer) args[0];
			Vec3d playerPos = player.getPos();
			List<String> regionsInRange = RegionManager.getInstance()
					.getRegionNames(region -> BoxUtils.getDistanceManhattan(region.getBoundingBox(), playerPos) <= range);

			if (regionsInRange.isEmpty()) {
				player.sendMessage(new TextUtils()
						.formatted(Formatting.GREEN).text("Found no regions within ")
						.formatted(Formatting.AQUA).text(Integer.toString(range))
						.formatted(Formatting.GREEN).text(" blocks.")
						.build());
				return 1;
			}

			player.sendMessage(new TextUtils()
					.formatted(Formatting.GREEN).text("Found ")
					.formatted(Formatting.AQUA).text(Integer.toString(regionsInRange.size()))
					.formatted(Formatting.GREEN).text(" regions within ")
					.formatted(Formatting.AQUA).text(Integer.toString(range))
					.formatted(Formatting.GREEN).text(" blocks. ")
					.list(", ", regionsInRange, Formatting.AQUA)
					.build());
			return 1;
		}
	}

	private enum TntCmd implements ITracerArgCommand {
		INSTANCE;
		private static final List<ArgDescriptor> ARG_DESCRIPTORS = List.of(
				new ArgDescriptor("name", StringArgumentType.string(), true)
		);

		@Override
		public String getCmdPrefix() {
			return "tnt";
		}

		@Override
		public List<ArgDescriptor> getArguments() {
			return ARG_DESCRIPTORS;
		}

		@Override
		public void help(TextUtils builder) {
			ITracerArgCommand.appendCommandDescriptor(this, builder);
			String nameArgName = ARG_DESCRIPTORS.get(0).name();
			builder.formatted(Formatting.WHITE).text(" - toggle tnt flag for region, or globally if '" + nameArgName + "' is omitted.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			boolean globalTnt = RegionManager.getInstance().toggleGlobalTnt();
			player.sendMessage(new TextUtils()
					.formatted(Formatting.GREEN).text("TNT outside of regions is now ")
					.formatted(globalTnt ? Formatting.RED : Formatting.AQUA).text(globalTnt ? "enabled" : "disabled")
					.formatted(Formatting.GREEN).text(".")
					.build());
			return 1;
		}

		@Override
		public int onExecute(ServerPlayerEntity player, Object[] args) {
			RegionManager regionManager = RegionManager.getInstance();
			String regionName = (String) args[0];

			Region region = regionManager.getRegion(regionName);
			if (region == null) {
				player.sendMessage(new TextUtils()
						.formatted(Formatting.RED).text("Region ")
						.formatted(Formatting.DARK_PURPLE).text(regionName)
						.formatted(Formatting.RED).text(" does not exist.")
						.build());
				return -1;
			}

			boolean tntDisabled = !region.isTntDisabled();
			region.setTntDisabled(tntDisabled);
			player.sendMessage(new TextUtils()
					.formatted(Formatting.GREEN).text("Tnt is now ")
					.formatted(tntDisabled ? Formatting.AQUA : Formatting.RED).text(tntDisabled ? "disabled" : "enabled")
					.formatted(Formatting.GREEN).text(" in region ")
					.formatted(Formatting.AQUA).text(regionName)
					.formatted(Formatting.GREEN).text(".")
					.build());
			return 1;
		}
	}
}
