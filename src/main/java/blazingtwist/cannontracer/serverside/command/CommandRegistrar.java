package blazingtwist.cannontracer.serverside.command;

import blazingtwist.cannontracer.serverside.command.impl.ActivatorCmd;
import blazingtwist.cannontracer.serverside.command.impl.CannonTesterCmd;
import blazingtwist.cannontracer.serverside.command.impl.RegionCmd;
import blazingtwist.cannontracer.serverside.command.impl.TracerCmd;
import blazingtwist.cannontracer.shared.utils.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class CommandRegistrar implements CommandRegistrationCallback {

	private static CommandRegistrar instance;

	public static CommandRegistrar getInstance() {
		if (instance == null) {
			instance = new CommandRegistrar();
		}
		return instance;
	}

	private final List<ITracerCommand> commands = new ArrayList<>();

	private CommandRegistrar() {
		this.commands.addAll(TracerCmd.INSTANCE.getCommands());
		this.commands.add(ActivatorCmd.INSTANCE);
		this.commands.add(CannonTesterCmd.INSTANCE);
		this.commands.add(RegionCmd.INSTANCE);

		CommandRegistrationCallback.EVENT.register(this);
	}

	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher,
						 CommandRegistryAccess registryAccess,
						 CommandManager.RegistrationEnvironment environment) {
		LiteralArgumentBuilder<ServerCommandSource> tracerLiteral = LiteralArgumentBuilder.literal("tracer_server");

		for (ITracerCommand command : commands) {
			command.register(tracerLiteral);
		}

		tracerLiteral.executes(ctx -> this.onHelpCommand(ctx.getSource().getPlayer()));
		LiteralCommandNode<ServerCommandSource> tracerLiteralNode = dispatcher.register(tracerLiteral);

		// set up cts alias
		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("cts")
				.redirect(tracerLiteralNode)
				.executes(ctx -> this.onHelpCommand(ctx.getSource().getPlayer())));
	}

	private int onHelpCommand(@Nullable ServerPlayerEntity player) {
		if (player == null) {
			return 0;
		}

		TextUtils messageBuilder = new TextUtils();
		messageBuilder.formatted(Formatting.GOLD).text("SubCommands of ")
				.formatted(Formatting.GOLD, Formatting.BOLD).text("/tracer_server")
				.formatted(Formatting.GOLD).text(" (alias ")
				.formatted(Formatting.GOLD, Formatting.BOLD).text("/cts")
				.formatted(Formatting.GOLD).text("):");

		for (ITracerCommand command : commands) {
			messageBuilder.lineBreak();
			command.help(messageBuilder);
		}

		player.sendMessage(messageBuilder.build());
		return 1;
	}

}
