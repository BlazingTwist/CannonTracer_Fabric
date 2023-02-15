package blazingtwist.cannontracer.clientside.gui.widgets;

import blazingtwist.cannontracer.shared.utils.ColorUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import java.util.function.Consumer;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class BetterTextField extends WTextField {

	private static final int BACKGROUND_COLOR = 0xFF707070;
	private static final int BORDER_COLOR_LOWLIGHT = 0xFF333337;
	private static final int BORDER_COLOR_HIGHLIGHT = 0xFFE3E3E4;
	private static final int SELECTED_COLOR_LOWLIGHT = 0xFFA3A337;
	private static final int SELECTED_COLOR_HIGHLIGHT = 0xFFFFFFB4;

	private int backgroundColor = BACKGROUND_COLOR;
	private int borderColorLowlight = BORDER_COLOR_LOWLIGHT;
	private int borderColorHighlight = BORDER_COLOR_HIGHLIGHT;
	private int selectedColorLowlight = SELECTED_COLOR_LOWLIGHT;
	private int selectedColorHighlight = SELECTED_COLOR_HIGHLIGHT;

	private Consumer<String> submitListener;

	public BetterTextField() {
	}

	public void setTint(int color) {
		backgroundColor = ColorUtils.multiply(BACKGROUND_COLOR, color);
		borderColorLowlight = ColorUtils.multiply(BORDER_COLOR_LOWLIGHT, color);
		borderColorHighlight = ColorUtils.multiply(BORDER_COLOR_HIGHLIGHT, color);
		selectedColorLowlight = ColorUtils.multiply(SELECTED_COLOR_LOWLIGHT, color);
		selectedColorHighlight = ColorUtils.multiply(SELECTED_COLOR_HIGHLIGHT, color);
	}

	public void setOnSubmitListener(Consumer<String> submittedValueConsumer) {
		submitListener = submittedValueConsumer;
	}

	@Override
	public void onFocusLost() {
		super.onDirectionalKey(1, 0);
		super.onFocusLost();
		if (submitListener != null) {
			submitListener.accept(this.getText());
		}
	}

	@Override
	protected void renderBox(MatrixStack matrices, int x, int y) {
		int borderLowLight = isFocused() ? selectedColorLowlight : borderColorLowlight;
		int borderHighLight = isFocused() ? selectedColorHighlight : borderColorHighlight;

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		ScreenDrawing.coloredRect(matrices, x, y, width, height, borderLowLight);
		ScreenDrawing.coloredRect(matrices, x + 1, y + 1, width - 1, height - 1, borderHighLight);
		ScreenDrawing.coloredRect(matrices, x + 1, y + 1, width - 2, height - 2, backgroundColor);
	}

	@Override
	public void onKeyPressed(int ch, int key, int modifiers) {
		super.onKeyPressed(ch, key, modifiers);
		if (ch == GLFW.GLFW_KEY_ENTER || ch == GLFW.GLFW_KEY_KP_ENTER) {
			this.releaseFocus();
		}
	}

	@Override
	public void setSize(int x, int y) {
		this.width = Math.max(x, 4);
		this.height = Math.max(y, 4);
	}
}
