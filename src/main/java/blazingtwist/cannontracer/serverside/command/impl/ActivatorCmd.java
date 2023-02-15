package blazingtwist.cannontracer.serverside.command.impl;

import blazingtwist.cannontracer.serverside.ActivatorPlayerManager;
import blazingtwist.cannontracer.serverside.command.ITracerCommand;
import blazingtwist.cannontracer.serverside.command.SubCommandRegistrar;
import blazingtwist.cannontracer.shared.utils.PlayerUtils;
import blazingtwist.cannontracer.shared.utils.TextUtils;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public enum ActivatorCmd implements SubCommandRegistrar {
	INSTANCE;

	private static final List<ITracerCommand> subCommands = List.of(
			ActivatorSelectCmd.INSTANCE,
			ActivatorTriggerCmd.INSTANCE
	);

	@Override
	public String getCmdPrefix() {
		return "activator";
	}

	@Override
	public void help(TextUtils builder) {
		ITracerCommand.appendCommandDescriptor(this, builder);
		builder.formatted(Formatting.WHITE).text(" - use blocks remotely.");
	}

	@Override
	public Iterable<ITracerCommand> getSubCommands() {
		return subCommands;
	}

	private enum ActivatorSelectCmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "select";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - select the looked at block for remote activation.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			BlockPos pos = PlayerUtils.getLookedAtBlockPos(player, 10);
			BlockState block = PlayerUtils.getBlockState(player, pos);
			if (block == null || block.isAir()) {
				player.sendMessage(Text.literal("Could not select block. Might be too far away.").formatted(Formatting.RED));
				return -1;
			}

			ActivatorPlayerManager.getInstance().playerSelectsBlock(player, pos);
			MutableText message = new TextUtils()
					.formatted(Formatting.GREEN).text("Selected block at location ")
					.formatted(Formatting.AQUA).text(pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
					.formatted(Formatting.GREEN).text(".")
					.build();
			player.sendMessage(message);
			return 1;
		}
	}

	private enum ActivatorTriggerCmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "trigger";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - remotely trigger the selected block.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			BlockPos selectedBlock = ActivatorPlayerManager.getInstance().getSelectedBlock(player);
			if (selectedBlock == null) {
				player.sendMessage(Text.literal("No block selected.").formatted(Formatting.RED));
				return -1;
			}

			BlockState targetBlock = PlayerUtils.getBlockState(player, selectedBlock);
			if (targetBlock == null) {
				player.sendMessage(Text.literal("No block found in selection.").formatted(Formatting.RED));
				return -1;
			}

			//noinspection deprecation
			targetBlock.getBlock().onUse(targetBlock, player.world, selectedBlock, player, Hand.MAIN_HAND, null);
			player.sendMessage(Text.literal("Block activated.").formatted(Formatting.GREEN));
			return 1;
		}
	}
}
