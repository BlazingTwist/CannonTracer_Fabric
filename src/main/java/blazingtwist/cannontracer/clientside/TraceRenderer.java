package blazingtwist.cannontracer.clientside;

import blazingtwist.cannontracer.clientside.datatype.Color;
import blazingtwist.cannontracer.clientside.datatype.EntityTrackingSettings;
import blazingtwist.cannontracer.clientside.datatype.TracerConfig;
import blazingtwist.cannontracer.clientside.datatype.TracesSaveData;
import blazingtwist.cannontracer.shared.datatypes.FinalVec3d;
import blazingtwist.cannontracer.shared.datatypes.MutableVec3d;
import blazingtwist.cannontracer.shared.utils.RenderUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;

public class TraceRenderer implements RenderController.LineRendererListener {

	private static TraceRenderer instance;

	public static TraceRenderer getInstance() {
		if (instance == null) {
			instance = new TraceRenderer();
		}
		return instance;
	}

	private TraceRenderer() {
		RenderController.getInstance().addRendererListener(this);
		WorldRenderEvents.BEFORE_ENTITIES.register(this::onBeforeEntitiesRendered);
	}

	private final HashMap<String, HashMap<TracePos, TickMetaData>> tracesByEntityType = new HashMap<>();

	public void clearTraces() {
		synchronized (tracesByEntityType) {
			tracesByEntityType.clear();
		}
	}

	public TracesSaveData getTracesSaveData() {
		synchronized (tracesByEntityType) {
			return new TracesSaveData(tracesByEntityType);
		}
	}

	public void addTraces(TracesSaveData saveData) {
		synchronized (tracesByEntityType) {
			for (Map.Entry<String, TracesSaveData.EntityTracesSaveData> savedTraceEntry : saveData.getSaveData().entrySet()) {
				HashMap<TracePos, TickMetaData> renderTraceData = tracesByEntityType.computeIfAbsent(savedTraceEntry.getKey(), et -> new HashMap<>());
				savedTraceEntry.getValue().loadToTraceRenderer(renderTraceData);
			}
		}
	}

	public void addTrace(String entityType, long startTick, FinalVec3d[] positions, FinalVec3d[] velocities) {
		synchronized (tracesByEntityType) {
			HashMap<TracePos, TickMetaData> entityTraceData = tracesByEntityType.computeIfAbsent(entityType, et -> new HashMap<>());

			// make renderer show the spawn tick velocity
			addTrace(entityTraceData, startTick, positions[0], positions[0], velocities[0], velocities[0], false);

			for (int i = 1; i < positions.length; i++) {
				long tick = startTick + i;
				boolean isDespawnTick = (i + 1) == positions.length;
				addTrace(entityTraceData, tick, positions[i - 1], positions[i], velocities[i - 1], velocities[i], isDespawnTick);
			}
		}
	}

	private void addTrace(HashMap<TracePos, TickMetaData> entityTraceData, long tick, FinalVec3d pos0, FinalVec3d pos1, FinalVec3d v0, FinalVec3d v1, boolean isDespawnTick) {
		boolean xFirst = Math.abs(v0.x()) >= Math.abs(v0.z());
		TracePos tracePos = new TracePos(pos0, pos1, xFirst);
		TickMetaData metaData = entityTraceData.computeIfAbsent(tracePos, x -> new TickMetaData());
		metaData.addTick(tick, v1);
		if (isDespawnTick) {
			metaData.despawnTicks.add(tick);
		}
	}

	public Set<Long> findDespawnTicks() {
		synchronized (tracesByEntityType) {
			return tracesByEntityType.values().stream()
					.flatMap(trace -> trace.values().stream()
							.flatMap(meta -> meta.despawnTicks.stream())
					)
					.collect(Collectors.toSet());
		}
	}

	public OptionalLong findMinTick() {
		synchronized (tracesByEntityType) {
			return tracesByEntityType.values().stream()
					.flatMapToLong(trace -> trace.values().stream()
							.flatMapToLong(meta -> meta.maxVelocitiesByTick.keySet().stream().mapToLong(x -> x))
					)
					.min();
		}
	}

	public OptionalLong findMaxTick() {
		synchronized (tracesByEntityType) {
			return tracesByEntityType.values().stream()
					.flatMapToLong(trace -> trace.values().stream()
							.flatMapToLong(meta -> meta.maxVelocitiesByTick.keySet().stream().mapToLong(x -> x))
					)
					.max();
		}
	}

	@Override
	public boolean wantsToDraw() {
		return !tracesByEntityType.isEmpty();
	}

	@Override
	public void onRenderWorldLines(RenderController.PlayerCameraLinesRenderer context) {
		drawTraces(context, SettingsManager.getInstance().getRenderTick());
	}

	private void drawTraces(RenderController.PlayerCameraLinesRenderer context, long renderTick) {
		if (tracesByEntityType.isEmpty()) {
			return;
		}

		renderTraceLines(context);
		renderTraceBoxes(context, renderTick);
		RenderSystem.lineWidth(1.0f);
	}

	private void onBeforeEntitiesRendered(WorldRenderContext context) {
		SettingsManager settingsManager = SettingsManager.getInstance();
		TracerConfig tracerConfig = settingsManager.getTracerConfig();
		if (!tracerConfig.isDrawPositionText() && !tracerConfig.isDrawVelocityText()) {
			return;
		}

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		VertexConsumerProvider.Immediate vertexConsumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		long renderTick = settingsManager.getSessionSettings().getRenderTick();

		boolean drawPos = tracerConfig.isDrawPositionText();
		boolean drawVel = tracerConfig.isDrawVelocityText();
		boolean drawPosAndVel = drawPos && drawVel;
		int positionTextOffset = drawPosAndVel ? -20 : 0;
		int velocityTextOffset = drawPosAndVel ? 20 : 0;
		MutableText posText = Text.literal("POS").formatted(Formatting.DARK_RED, Formatting.BOLD);
		MutableText velText = Text.literal("VEL").formatted(Formatting.DARK_RED, Formatting.BOLD);
		DecimalFormat decimalFormat = tracerConfig.buildRenderDecimalFormat();
		float fontScale = 0.01f * tracerConfig.getDrawTextScale();

		MatrixStack matrixStack = context.matrixStack();
		matrixStack.push();
		matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);

		synchronized (tracesByEntityType) {
			for (Map.Entry<String, HashMap<TracePos, TickMetaData>> traceEntry : tracesByEntityType.entrySet()) {
				EntityTrackingSettings entitySettings = settingsManager.getEntitySettings(traceEntry.getKey());
				if (entitySettings == null || !entitySettings.isRender()) {
					continue;
				}
				float hitBoxRadius = (float) entitySettings.getHitBoxRadius();

				for (Map.Entry<TracePos, TickMetaData> posEntry : traceEntry.getValue().entrySet()) {
					if (!posEntry.getValue().maxVelocitiesByTick.containsKey(renderTick)) {
						continue;
					}

					TracePos pos = posEntry.getKey();
					MutableVec3d endVelocity = posEntry.getValue().maxVelocitiesByTick.get(renderTick);

					matrixStack.push();
					matrixStack.translate(pos.toPosition.x(), pos.toPosition.y() + hitBoxRadius, pos.toPosition.z());
					matrixStack.multiply(camera.getRotation());
					matrixStack.scale(-fontScale, -fontScale, fontScale);

					Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

					if (drawPos) {
						drawRightAlignedString(textRenderer, posText, positionTextOffset - 14, positionMatrix, vertexConsumer);
						drawDotCenteredNumber(textRenderer, pos.toPosition.x(), decimalFormat, positionTextOffset - 4, positionMatrix, vertexConsumer);
						drawDotCenteredNumber(textRenderer, pos.toPosition.y(), decimalFormat, positionTextOffset + 5, positionMatrix, vertexConsumer);
						drawDotCenteredNumber(textRenderer, pos.toPosition.z(), decimalFormat, positionTextOffset + 14, positionMatrix, vertexConsumer);
					}

					if (drawVel) {
						drawRightAlignedString(textRenderer, velText, velocityTextOffset - 14, positionMatrix, vertexConsumer);
						drawDotCenteredNumber(textRenderer, endVelocity.getX(), decimalFormat, velocityTextOffset - 4, positionMatrix, vertexConsumer);
						drawDotCenteredNumber(textRenderer, endVelocity.getY(), decimalFormat, velocityTextOffset + 5, positionMatrix, vertexConsumer);
						drawDotCenteredNumber(textRenderer, endVelocity.getZ(), decimalFormat, velocityTextOffset + 14, positionMatrix, vertexConsumer);
					}

					matrixStack.pop();
				}
			}
		}

		matrixStack.pop();
	}

	private void drawRightAlignedString(TextRenderer renderer, Text text, float yOffset, Matrix4f positionMatrix,
										VertexConsumerProvider.Immediate vertexConsumer) {
		float xOffset = renderer.getWidth(text);
		renderer.draw(text, -xOffset, yOffset, -1, false, positionMatrix, vertexConsumer, false, 0, 0x00F0_00F0);
	}

	private void drawDotCenteredNumber(TextRenderer renderer, double number, DecimalFormat format, float yOffset, Matrix4f positionMatrix,
									   VertexConsumerProvider.Immediate vertexConsumer) {
		String text = format.format(number);
		int dotIndex = text.indexOf('.');
		int xOffset = dotIndex >= 0
				? -renderer.getWidth(text.substring(0, dotIndex))
				: -renderer.getWidth(text);
		renderer.draw(text, xOffset, yOffset, -1, false, positionMatrix, vertexConsumer, false, 0, 0x00F0_00F0);
	}

	private void renderTraceLines(RenderController.PlayerCameraLinesRenderer context) {
		synchronized (tracesByEntityType) {
			for (Map.Entry<String, HashMap<TracePos, TickMetaData>> tracesEntry : tracesByEntityType.entrySet()) {
				String entityTypeName = tracesEntry.getKey();
				EntityTrackingSettings entitySettings = SettingsManager.getInstance().getEntitySettings(entityTypeName);
				if (entitySettings == null || !entitySettings.isRender()) {
					continue;
				}

				Color color = entitySettings.getColor();
				double boxRadius = entitySettings.getHitBoxRadius();

				BufferBuilder bufferBuilder = context.getBufferBuilder();
				bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
				RenderSystem.lineWidth(entitySettings.getThickness());

				for (TracePos tracePos : tracesEntry.getValue().keySet()) {
					FinalVec3d pos0 = tracePos.fromPosition;
					FinalVec3d pos1 = tracePos.toPosition;

					RenderUtils.pushYLine(bufferBuilder, pos0.x(), pos0.y() + boxRadius, pos1.y() + boxRadius, pos0.z(), color);
					if (tracePos.xFirst) {
						RenderUtils.pushXLine(bufferBuilder, pos0.x(), pos1.x(), pos1.y() + boxRadius, pos0.z(), color);
						RenderUtils.pushZLine(bufferBuilder, pos1.x(), pos1.y() + boxRadius, pos0.z(), pos1.z(), color);
					} else {
						RenderUtils.pushZLine(bufferBuilder, pos0.x(), pos1.y() + boxRadius, pos0.z(), pos1.z(), color);
						RenderUtils.pushXLine(bufferBuilder, pos0.x(), pos1.x(), pos1.y() + boxRadius, pos1.z(), color);
					}
				}

				context.getTessellator().draw();
			}
		}
	}

	private void renderTraceBoxes(RenderController.PlayerCameraLinesRenderer context, long tick) {
		synchronized (tracesByEntityType) {
			for (Map.Entry<String, HashMap<TracePos, TickMetaData>> tracesEntry : tracesByEntityType.entrySet()) {
				String entityTypeName = tracesEntry.getKey();
				EntityTrackingSettings entitySettings = SettingsManager.getInstance().getEntitySettings(entityTypeName);
				if (entitySettings == null || !entitySettings.isRender()) {
					continue;
				}

				Color color = entitySettings.getColor();
				Color invColor = color.copy().invert();
				double boxRadius = entitySettings.getHitBoxRadius();

				BufferBuilder bufferBuilder = context.getBufferBuilder();
				bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
				RenderSystem.lineWidth(entitySettings.getThickness());

				for (Map.Entry<TracePos, TickMetaData> traceEntry : tracesEntry.getValue().entrySet()) {
					boolean isDespawnTrace = traceEntry.getValue().despawnTicks.contains(tick);
					boolean drawTrace = isDespawnTrace || traceEntry.getValue().maxVelocitiesByTick.containsKey(tick);
					if (!drawTrace) {
						continue;
					}

					RenderUtils.drawHitBox(bufferBuilder, traceEntry.getKey().toPosition, isDespawnTrace ? invColor : color, boxRadius);
				}

				context.getTessellator().draw();
			}
		}
	}

	public static record TracePos(
			@JsonProperty("pos0") FinalVec3d fromPosition,
			@JsonProperty("pos1") FinalVec3d toPosition,
			@JsonProperty("xf") boolean xFirst) {
	}

	public static class TickMetaData {
		@JsonProperty("dt")
		public final HashSet<Long> despawnTicks = new HashSet<>();

		/**
		 * Contains the occupied ticks mapped to the max velocity observed during that tick
		 */
		@JsonProperty("v1")
		public final HashMap<Long, MutableVec3d> maxVelocitiesByTick = new HashMap<>();

		public void addTick(long tick, FinalVec3d velocity) {
			MutableVec3d existingVelocity = maxVelocitiesByTick.computeIfAbsent(tick, x -> new MutableVec3d());
			existingVelocity.setMax(velocity);
		}

		public void merge(TickMetaData other) {
			this.despawnTicks.addAll(other.despawnTicks);
			for (Map.Entry<Long, MutableVec3d> otherTickEntry : other.maxVelocitiesByTick.entrySet()) {
				MutableVec3d existingVelocity = maxVelocitiesByTick.get(otherTickEntry.getKey());
				if (existingVelocity == null) {
					maxVelocitiesByTick.put(otherTickEntry.getKey(), otherTickEntry.getValue());
				} else {
					existingVelocity.setMax(otherTickEntry.getValue());
				}
			}
		}
	}

}
