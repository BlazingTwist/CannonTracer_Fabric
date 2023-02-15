package blazingtwist.cannontracer.serverside.command;

import blazingtwist.cannontracer.shared.utils.TextUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public interface SubCommandRegistrar extends ITracerCommand {
	Iterable<ITracerCommand> getSubCommands();

	@Override
	default void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		LiteralArgumentBuilder<ServerCommandSource> activatorLiteral = LiteralArgumentBuilder
				.<ServerCommandSource>literal(getCmdPrefix())
				.executes(ctx -> ITracerCommand.executePlayerCommand(ctx, this::onExecute));

		for (ITracerCommand subCommand : getSubCommands()) {
			subCommand.register(activatorLiteral);
		}

		builder.then(activatorLiteral);
	}

	@Override
	default int onExecute(ServerPlayerEntity player) {
		TextUtils messageBuilder = new TextUtils();
		messageBuilder.formatted(Formatting.GOLD).text("SubCommands of ");
		messageBuilder.formatted(Formatting.GOLD, Formatting.BOLD).text(getCmdPrefix());
		messageBuilder.formatted(Formatting.GOLD).text(":");

		for (ITracerCommand subCommand : getSubCommands()) {
			messageBuilder.lineBreak();
			subCommand.help(messageBuilder);
		}

		player.sendMessage(messageBuilder.build());
		return 1;
	}
}
