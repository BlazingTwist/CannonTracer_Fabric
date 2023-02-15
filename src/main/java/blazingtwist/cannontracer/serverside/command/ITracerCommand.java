package blazingtwist.cannontracer.serverside.command;

import blazingtwist.cannontracer.shared.utils.TextUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.function.Function;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public interface ITracerCommand {
	String getCmdPrefix();

	default void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		builder.then(LiteralArgumentBuilder.<ServerCommandSource>literal(getCmdPrefix())
				.requires(ServerCommandSource::isExecutedByPlayer)
				.executes(ctx -> ITracerCommand.executePlayerCommand(ctx, this::onExecute)));
	}

	void help(TextUtils builder);

	int onExecute(ServerPlayerEntity player);

	static void appendCommandDescriptor(ITracerCommand instance, TextUtils builder) {
		builder.formatted(Formatting.AQUA).text(instance.getCmdPrefix());
	}

	static int executePlayerCommand(CommandContext<ServerCommandSource> context, Function<ServerPlayerEntity, Integer> command) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		if (player == null) {
			return 0;
		}
		return command.apply(player);
	}
}
