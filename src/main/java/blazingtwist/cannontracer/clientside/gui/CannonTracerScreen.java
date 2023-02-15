package blazingtwist.cannontracer.clientside.gui;

import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;

public class CannonTracerScreen extends CottonClientScreen {

	public static void openScreen(Vec3i commandPos, TestCannonData cannon) {
		RenderSystem.recordRenderCall(() -> {
			CannonTesterGui gui = CannonTesterGui.getInstance();
			gui.loadData(commandPos, cannon);
			MinecraftClient.getInstance().setScreen(new CannonTracerScreen(gui));
		});
	}

	public CannonTracerScreen(GuiDescription description) {
		super(description);
	}

	@Override
	public void removed() {
		WWidget focus = this.description.getFocus();
		if (focus != null) {
			this.description.releaseFocus(focus);
		}

		if(this.description instanceof IOnCloseListener listener) {
			listener.onClose();
		}

		super.removed();
	}

	@Override
	protected void reposition(int screenWidth, int screenHeight) {
		if (description == null) {
			return;
		}

		WPanel rootPanel = description.getRootPanel();
		if (rootPanel == null) {
			return;
		}

		this.left = (screenWidth - rootPanel.getWidth()) / 2;
		this.top = (screenHeight - rootPanel.getHeight()) / 2;
	}
}
