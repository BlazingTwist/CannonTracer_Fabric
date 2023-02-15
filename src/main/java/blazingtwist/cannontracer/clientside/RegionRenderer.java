package blazingtwist.cannontracer.clientside;

import blazingtwist.cannontracer.clientside.datatype.Color;
import blazingtwist.cannontracer.shared.utils.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Box;

public enum RegionRenderer implements RenderController.LineRendererListener {
	INSTANCE;

	public static RegionRenderer getInstance() {
		return RegionRenderer.INSTANCE;
	}

	RegionRenderer() {
		RenderController.getInstance().addRendererListener(this);
	}

	private final List<Box> regions = new ArrayList<>();

	public void clearRegions() {
		synchronized (regions) {
			regions.clear();
		}
	}

	public void addRegion(Box region) {
		synchronized (regions) {
			regions.add(region);
		}
	}

	@Override
	public boolean wantsToDraw() {
		return !regions.isEmpty();
	}

	@Override
	public void onRenderWorldLines(RenderController.PlayerCameraLinesRenderer context) {
		if (regions.isEmpty()) {
			return;
		}

		BufferBuilder bufferBuilder = context.getBufferBuilder();
		bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		RenderSystem.lineWidth(5.0f);
		drawRegionBoxes(bufferBuilder);
		context.getTessellator().draw();
		RenderSystem.lineWidth(1.0f);
	}

	private void drawRegionBoxes(BufferBuilder bufferBuilder) {
		Color boxColor = new Color(255, 128, 0, 255);
		synchronized (regions) {
			for (Box region : regions) {
				RenderUtils.pushBox(bufferBuilder, region, boxColor);
			}
		}
	}
}
