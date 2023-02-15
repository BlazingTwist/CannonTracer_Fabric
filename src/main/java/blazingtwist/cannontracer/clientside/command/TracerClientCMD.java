package blazingtwist.cannontracer.clientside.command;

import blazingtwist.cannontracer.CannonTracerMod;
import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.TraceRenderer;
import blazingtwist.cannontracer.clientside.datatype.TracesSaveData;
import blazingtwist.cannontracer.clientside.gui.CannonTracerScreen;
import blazingtwist.cannontracer.clientside.gui.TracerConfigGui;
import blazingtwist.cannontracer.networking.ClientPacketHandler;
import blazingtwist.cannontracer.shared.FileManager;
import blazingtwist.cannontracer.shared.utils.TextUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TracerClientCMD {

	private static TracerClientCMD instance;

	public static TracerClientCMD getInstance() {
		if (instance == null) {
			instance = new TracerClientCMD();
		}
		return instance;
	}

	private TracerClientCMD() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> tracerLiteral = LiteralArgumentBuilder.literal("tracer_client");
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("gui").executes(ctx -> this.command_TracerGUI()));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("pull").executes(ctx -> this.command_TracerPull()));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("clear").executes(ctx -> this.command_TracerClear()));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("tick")
					.then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("int", IntegerArgumentType.integer())
							.executes(ctx -> this.command_TracerTick(IntegerArgumentType.getInteger(ctx, "int")))
					).executes(ctx -> this.command_TracerTick(0)));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("list").executes(ctx -> this.command_tracerList(ctx.getSource().getPlayer())));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("save")
					.then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("name", StringArgumentType.string())
							.executes(ctx -> this.command_tracerSave(ctx.getSource().getPlayer(), StringArgumentType.getString(ctx, "name")))
					).executes(ctx -> {
						throw new SimpleCommandExceptionType(Text.literal("Received no trace name.")).create();
					}));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("load")
					.then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("names", StringArgumentType.string())
							.executes(ctx -> this.command_tracerLoad(ctx.getSource().getPlayer(), StringArgumentType.getString(ctx, "names").split(",")))
					).executes(ctx -> {
						throw new SimpleCommandExceptionType(Text.literal("Received no trace names.")).create();
					}));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("delete")
					.then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("name", StringArgumentType.string())
							.executes(ctx -> this.command_tracerDelete(ctx.getSource().getPlayer(), StringArgumentType.getString(ctx, "name")))
					).executes(ctx -> {
						throw new SimpleCommandExceptionType(Text.literal("Received no trace name.")).create();
					}));
			tracerLiteral.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("help").executes(ctx -> this.command_TracerHelp(ctx.getSource().getPlayer())));
			tracerLiteral.executes(ctx -> this.command_TracerHelp(ctx.getSource().getPlayer()));

			LiteralCommandNode<FabricClientCommandSource> tracerLiteralNode = dispatcher.register(tracerLiteral);

			// set up ctc <-> tracer_client alias.
			dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("ctc")
					.redirect(tracerLiteralNode)
					.executes(ctx -> this.command_TracerHelp(ctx.getSource().getPlayer())));
		});
	}

	private int command_TracerGUI() {
		RenderSystem.recordRenderCall(() -> MinecraftClient.getInstance().setScreen(new CannonTracerScreen(TracerConfigGui.getInstance())));
		return 1;
	}

	private int command_TracerPull() {
		ClientPacketHandler.getInstance().clientToServer_requestTraceData();
		return 1;
	}

	private int command_TracerClear() {
		TraceRenderer.getInstance().clearTraces();
		return 1;
	}

	private int command_TracerTick(long tick) {
		SettingsManager.getInstance().getSessionSettings().setRenderTick(tick);
		return 1;
	}

	private int command_tracerList(ClientPlayerEntity player) throws CommandSyntaxException {
		List<String> subFiles = FileManager.listSubFiles(FileManager.traceDirectoryPrefix);
		if (subFiles == null) {
			throw new SimpleCommandExceptionType(Text.literal("Failed to read from file-system.")).create();
		}

		if (subFiles.isEmpty()) {
			player.sendMessage(Text.literal("No files found. (Are you sure you saved any?)").formatted(Formatting.RED));
			return -1;
		}

		TextUtils textUtils = new TextUtils();
		textUtils.formatted(Formatting.WHITE).text("Names: ");
		boolean wroteFirst = false;
		for (String subFile : subFiles) {
			if (wroteFirst) {
				textUtils.formatted(Formatting.WHITE).text(", ");
			}
			textUtils.formatted(Formatting.AQUA).text(subFile);
			wroteFirst = true;
		}
		player.sendMessage(textUtils.build());
		return 1;
	}

	private int command_tracerSave(ClientPlayerEntity player, String name) throws CommandSyntaxException {
		if (name == null || name.isBlank()) {
			throw new SimpleCommandExceptionType(Text.literal("Received no trace name.")).create();
		}

		if (FileManager.containsSpecialChars(name)) {
			throw new SimpleCommandExceptionType(Text.literal("Trace name may not contain special characters (\\/.)")).create();
		}

		TracesSaveData tracesSaveData = TraceRenderer.getInstance().getTracesSaveData();
		String json = FileManager.objectToJson(tracesSaveData);
		if (json == null) {
			throw new SimpleCommandExceptionType(Text.literal("Failed to serialize traces!")).create();
		}

		if (!FileManager.saveToFile(FileManager.traceDirectoryPrefix + name + FileManager.traceFileExtension, json)) {
			throw new SimpleCommandExceptionType(Text.literal("Failed to write to file!")).create();
		}

		player.sendMessage(Text.literal("trace '" + name + "' saved.").formatted(Formatting.GREEN));
		return 1;
	}

	private int command_tracerLoad(ClientPlayerEntity player, String[] names) throws CommandSyntaxException {
		if (names == null || names.length <= 0) {
			throw new SimpleCommandExceptionType(Text.literal("Received no trace names.")).create();
		}

		for (String name : names) {
			String fullName = FileManager.traceDirectoryPrefix + name + FileManager.traceFileExtension;
			if (!FileManager.fileExists(fullName)) {
				throw new SimpleCommandExceptionType(Text.literal("Trace file '" + name + "' does not exist!")).create();
			}
			String json = FileManager.readFileToString(fullName);
			if (json == null) {
				throw new SimpleCommandExceptionType(Text.literal("Unable to read from trace file '" + name + "'.")).create();
			}

			try {
				TracesSaveData saveData = FileManager.parseJsonToObject(json, new TypeReference<>() {});
				TraceRenderer.getInstance().addTraces(saveData);
			} catch (JsonProcessingException e) {
				CannonTracerMod.LOGGER.error("Failed to parse trace file '{}'", name, e);
				throw new SimpleCommandExceptionType(Text.literal("Failed to parse trace file '" + name + "'")).create();
			}
		}

		player.sendMessage(Text.literal("Finished loading " + names.length + " trace files.").formatted(Formatting.GREEN));
		return 1;
	}

	private int command_tracerDelete(ClientPlayerEntity player, String name) throws CommandSyntaxException {
		if (name == null || name.isBlank()) {
			throw new SimpleCommandExceptionType(Text.literal("Received no trace name.")).create();
		}

		String fullName = FileManager.traceDirectoryPrefix + name + FileManager.traceFileExtension;
		if (!FileManager.fileExists(fullName)) {
			throw new SimpleCommandExceptionType(Text.literal("Trace file '" + name + "' does not exist.")).create();
		}

		if (FileManager.deleteFile(fullName)) {
			player.sendMessage(Text.literal("Trace '" + name + "' deleted.").formatted(Formatting.GREEN));
			return -1;
		} else {
			player.sendMessage(new TextUtils()
					.formatted(Formatting.RED).text("Unable to delete file ")
					.formatted(Formatting.LIGHT_PURPLE).text(name)
					.formatted(Formatting.RED).text(". FileSystem path: ")
					.formatted(Formatting.LIGHT_PURPLE).text(FileManager.resolveSubPath(fullName).toString())
					.build()
			);
			return 1;
		}
	}

	private int command_TracerHelp(ClientPlayerEntity player) {
		MutableText message = new TextUtils()
				.formatted(Formatting.GOLD).text("SubCommands of ")
				.formatted(Formatting.GOLD, Formatting.BOLD).text("/tracer_client")
				.formatted(Formatting.GOLD).text(" (alias ")
				.formatted(Formatting.GOLD, Formatting.BOLD).text("/ctc")
				.formatted(Formatting.GOLD).text("):\n")

				.formatted(Formatting.AQUA).text("/ctc gui").formatted(Formatting.WHITE).text(" - open the config gui.\n")
				.formatted(Formatting.AQUA).text("/ctc pull").formatted(Formatting.WHITE).text(" - pull traces from server.\n")
				.formatted(Formatting.AQUA).text("/ctc clear").formatted(Formatting.WHITE).text(" - clear the current traces.\n")
				.formatted(Formatting.AQUA).text("/ctc tick ").formatted(Formatting.DARK_AQUA).text("<int>").formatted(Formatting.WHITE).text(" - set display tick.\n")
				.formatted(Formatting.AQUA).text("/ctc list").formatted(Formatting.WHITE).text(" - list saved traces.\n")
				.formatted(Formatting.AQUA).text("/ctc save ").formatted(Formatting.DARK_AQUA).text("\"<name>\"").formatted(Formatting.WHITE).text(" - save the current traces.\n")
				.formatted(Formatting.AQUA).text("/ctc load ").formatted(Formatting.DARK_AQUA).text("\"<name_1>,...,<name_n>\"").formatted(Formatting.WHITE).text(" - load one or more saved traces.\n")
				.formatted(Formatting.AQUA).text("/ctc delete ").formatted(Formatting.DARK_AQUA).text("\"name\"").formatted(Formatting.WHITE).text(" - delete a saved trace.")
				.build();
		player.sendMessage(message);
		return 1;
	}
}
