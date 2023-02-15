package blazingtwist.cannontracer.serverside.command.impl;

import blazingtwist.cannontracer.serverside.TracerPlayerManager;
import blazingtwist.cannontracer.serverside.command.ITracerCommand;
import blazingtwist.cannontracer.shared.utils.TextUtils;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum TracerCmd {
	INSTANCE;

	public Collection<ITracerCommand> getCommands() {
		return List.of(RegisterCmd.INSTANCE, UnregisterCmd.INSTANCE, PlayersCmd.INSTANCE);
	}

	private enum RegisterCmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "register";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - register with this server.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			if (TracerPlayerManager.getInstance().isPlayerRegistered(player)) {
				player.sendMessage(Text.literal("You are already registered.").formatted(Formatting.RED));
				return 0;
			}

			TracerPlayerManager.getInstance().registerPlayer(player);
			player.sendMessage(Text.literal("You are now registered.").formatted(Formatting.GREEN));
			return 1;
		}
	}

	private enum UnregisterCmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "unregister";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - unregister from this server.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			if (!TracerPlayerManager.getInstance().isPlayerRegistered(player)) {
				player.sendMessage(Text.literal("You are not registered.").formatted(Formatting.RED));
				return 0;
			}

			TracerPlayerManager.getInstance().unregisterPlayer(player);
			player.sendMessage(Text.literal("You are no longer registered.").formatted(Formatting.GREEN));
			return 1;
		}
	}

	private enum PlayersCmd implements ITracerCommand {
		INSTANCE;

		@Override
		public String getCmdPrefix() {
			return "players";
		}

		@Override
		public void help(TextUtils builder) {
			ITracerCommand.appendCommandDescriptor(this, builder);
			builder.formatted(Formatting.WHITE).text(" - list all registered players.");
		}

		@Override
		public int onExecute(ServerPlayerEntity player) {
			Set<ServerPlayerEntity> registeredPlayers = TracerPlayerManager.getInstance().getRegisteredPlayers();
			if (registeredPlayers.isEmpty()) {
				player.sendMessage(Text.literal("No players are registered yet.").formatted(Formatting.GREEN));
			} else {
				MutableText message = Text.empty();
				message.append(Text.literal("Registered Players: ").formatted(Formatting.GREEN));

				boolean firstListElement = true;
				for (ServerPlayerEntity registeredPlayer : registeredPlayers) {
					if (!firstListElement) {
						message.append(Text.literal(", ").formatted(Formatting.GREEN));
					}
					message.append(registeredPlayer.getDisplayName());
					firstListElement = false;
				}

				player.sendMessage(message);
			}
			return 1;
		}
	}

}
