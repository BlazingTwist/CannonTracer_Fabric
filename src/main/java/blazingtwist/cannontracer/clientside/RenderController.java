package blazingtwist.cannontracer.clientside;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public enum RenderController {
	INSTANCE;

	public static RenderController getInstance() {
		return RenderController.INSTANCE;
	}

	private final List<LineRendererListener> lineRendererListeners = new ArrayList<>();

	public void addRendererListener(LineRendererListener listener) {
		lineRendererListeners.add(listener);
	}

	public void onWorldRendered() {
		if (lineRendererListeners.stream().noneMatch(LineRendererListener::wantsToDraw)) {
			return;
		}

		PlayerCameraLinesRenderer context = new PlayerCameraLinesRenderer();
		if (!context.loaded) {
			return;
		}

		for (LineRendererListener listener : lineRendererListeners) {
			listener.onRenderWorldLines(context);
		}
		context.unload();
	}

	public interface LineRendererListener {
		boolean wantsToDraw();

		void onRenderWorldLines(PlayerCameraLinesRenderer context);
	}

	public static class PlayerCameraLinesRenderer {
		private boolean loaded = false;
		private BufferBuilder bufferBuilder = null;
		private Tessellator tessellator = null;
		private MatrixStack matrixStack = null;

		public PlayerCameraLinesRenderer() {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			ClientPlayerEntity player = minecraft.player;
			if (player == null) {
				return;
			}

			Camera camera = minecraft.gameRenderer.getCamera();
			tessellator = Tessellator.getInstance();
			bufferBuilder = tessellator.getBuffer();

			if (SettingsManager.getInstance().xRayTraces()) {
				RenderSystem.disableDepthTest();
			}
			RenderSystem.depthMask(true);
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableTexture();

			matrixStack = RenderSystem.getModelViewStack();
			matrixStack.push();
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180f));
			matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
			RenderSystem.applyModelViewMatrix();
			RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

			loaded = true;
		}

		public BufferBuilder getBufferBuilder() {
			return bufferBuilder;
		}

		public Tessellator getTessellator() {
			return tessellator;
		}

		public MatrixStack getMatrixStack() {
			return matrixStack;
		}

		public void unload() {
			if (!loaded) {
				return;
			}

			matrixStack.pop();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			RenderSystem.enableTexture();
		}
	}

}
