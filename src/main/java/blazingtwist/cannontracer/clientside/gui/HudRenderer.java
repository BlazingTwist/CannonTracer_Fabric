package blazingtwist.cannontracer.clientside.gui;

import blazingtwist.cannontracer.clientside.SettingsManager;
import blazingtwist.cannontracer.clientside.datatype.HudConfig;
import blazingtwist.cannontracer.clientside.datatype.SessionSettings;
import blazingtwist.cannontracer.clientside.datatype.TracerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class HudRenderer {

	private static final int COLOR_NEUTRAL = 0x7f_ff_ff_ff;
	private static final int COLOR_ENABLED = 0x7f_00_ff_00;
	private static final int COLOR_DISABLED = 0x7f_ff_00_00;

	private static HudRenderer instance;

	public static HudRenderer getInstance() {
		if (instance == null) {
			instance = new HudRenderer();
		}
		return instance;
	}

	private HudRenderer() {
		HudRenderCallback.EVENT.register(this::onHudRender);
	}

	private void onHudRender(MatrixStack matrixStack, float tickDelta) {
		SettingsManager settingsManager = SettingsManager.getInstance();
		TracerConfig config = settingsManager.getTracerConfig();
		SessionSettings sessionSettings = settingsManager.getSessionSettings();
		HudConfig hudConfig = config.getHudConfig();
		if (!hudConfig.isEnabled()) {
			return;
		}
		if (hudConfig.getScale() <= 0) {
			return;
		}

		Window window = MinecraftClient.getInstance().getWindow();
		TextRenderer font = MinecraftClient.getInstance().textRenderer;
		float baseX = hudConfig.getXOffset() * window.getScaledWidth() / hudConfig.getScale();
		float baseY = hudConfig.getYOffset() * window.getScaledHeight() / hudConfig.getScale();

		matrixStack.push();
		matrixStack.scale(hudConfig.getScale(), hudConfig.getScale(), 1);

		long renderTick = sessionSettings.getRenderTick();
		boolean xRay = config.isXRayTraces();
		boolean positionText = config.isDrawPositionText();
		boolean velocityText = config.isDrawVelocityText();
		HudConfig.Alignment alignment = hudConfig.getAlignment();
		drawAligned(matrixStack, "X-Ray Traces: ", xRay, alignment, font, baseX, baseY);
		drawAligned(matrixStack, "Position Text: ", positionText, alignment, font, baseX, baseY + 10);
		drawAligned(matrixStack, "Velocity Text: ", velocityText, alignment, font, baseX, baseY + 20);
		drawAligned(matrixStack, "Display Tick: ", renderTick, alignment, font, baseX, baseY + 30);

		matrixStack.pop();
	}

	private void drawAligned(MatrixStack matrices, String label, long value, HudConfig.Alignment align, TextRenderer font, float x, float y) {
		drawAligned(matrices, label + value, align, font, x, y, COLOR_NEUTRAL);
	}

	private void drawAligned(MatrixStack matrices, String label, boolean value, HudConfig.Alignment align, TextRenderer font, float x, float y) {
		drawAligned(matrices, label + value, align, font, x, y, value ? COLOR_ENABLED : COLOR_DISABLED);
	}

	private void drawAligned(MatrixStack matrices, String text, HudConfig.Alignment alignment, TextRenderer font, float x, float y, int color) {
		float xOffset = switch (alignment) {
			case LEFT -> 0;
			case CENTER -> font.getWidth(text) / 2f;
			case RIGHT -> font.getWidth(text);
		};

		VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		Matrix4f posMat = matrices.peek().getPositionMatrix();
		font.draw(text, x - xOffset, y, color, true, posMat, immediate, false, 0, 0xF000F0, false);
		immediate.draw();
	}
}
