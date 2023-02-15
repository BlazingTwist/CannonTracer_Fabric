package blazingtwist.cannontracer.serverside.command;

import blazingtwist.cannontracer.shared.utils.TextUtils;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public interface ITracerArgCommand extends ITracerCommand {

	List<ArgDescriptor> getArguments();

	int onExecute(ServerPlayerEntity player, Object[] args);

	@Override
	default void register(LiteralArgumentBuilder<ServerCommandSource> builder) {
		LiteralArgumentBuilder<ServerCommandSource> baseLiteral = LiteralArgumentBuilder.literal(getCmdPrefix());

		baseLiteral.requires(ServerCommandSource::isExecutedByPlayer)
				.executes(ctx -> ITracerCommand.executePlayerCommand(ctx, this::onExecute));

		ArgumentBuilder<ServerCommandSource, ?> argBuilder = baseLiteral;
		List<ArgDescriptor> arguments = getArguments();
		for (int i = 0; i < arguments.size(); i++) {
			ArgDescriptor arg = arguments.get(i);
			final int numArgsConsumed = i + 1;
			RequiredArgumentBuilder<ServerCommandSource, ?> argNode = RequiredArgumentBuilder.argument(arg.name, arg.argumentType);
			argNode.executes(ctx -> executeArgCommand(ctx, numArgsConsumed, arguments, this::onExecute));
			argBuilder.then(argNode);
			argBuilder = argNode;
		}

		builder.then(baseLiteral);
	}

	static void appendCommandDescriptor(ITracerArgCommand instance, TextUtils builder) {
		builder.formatted(Formatting.AQUA).text(instance.getCmdPrefix());
		builder.formatted(Formatting.DARK_AQUA);
		for (ArgDescriptor arg : instance.getArguments()) {
			if (arg.optional) {
				builder.text(" [" + arg.name + "]");
			} else {
				builder.text(" <" + arg.name + ">");
			}
		}
	}

	static int executeArgCommand(CommandContext<ServerCommandSource> context,
								 int numArguments, List<ArgDescriptor> argDescriptors,
								 BiFunction<ServerPlayerEntity, Object[], Integer> command) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		if (player == null) {
			return 0;
		}

		Object[] argInstances = new Object[numArguments];
		for (int i = 0; i < numArguments; i++) {
			argInstances[i] = context.getArgument(argDescriptors.get(i).name, Object.class);
		}
		return command.apply(player, argInstances);
	}

	record ArgDescriptor(String name, ArgumentType<?> argumentType, boolean optional) {
	}
}
