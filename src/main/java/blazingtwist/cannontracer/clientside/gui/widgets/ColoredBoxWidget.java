package blazingtwist.cannontracer.clientside.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

public class ColoredBoxWidget extends WWidget {

	private int color;
	@Nullable
	private Integer borderColor = null;
	private int borderWidth = 1;

	public ColoredBoxWidget() {
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setBorderColor(@Nullable Integer borderColor) {
		this.borderColor = borderColor;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		if (borderColor != null) {
			ScreenDrawing.coloredRect(matrices, x, y, width, height, borderColor);
			ScreenDrawing.coloredRect(
					matrices, x + borderWidth, y + borderWidth,
					width - (2 * borderWidth), height - (2 * borderWidth), color
			);
		} else {
			ScreenDrawing.coloredRect(matrices, x, y, width, height, color);
		}
	}

	@Override
	public boolean canFocus() {
		return false;
	}

	@Override
	public boolean canResize() {
		return true;
	}
}
