package blazingtwist.cannontracer.clientside.gui.widgets;

import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class LabelWithShadow extends WLabel {

	private boolean textShadow = true;
	private Integer backgroundColor = null;
	private int horizontalMargin = 0;

	public LabelWithShadow(Text text) {
		super(text);
	}

	public void setTextShadow(boolean textShadow) {
		this.textShadow = textShadow;
	}

	public void setBackgroundColor(@Nullable Integer backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setHorizontalMargin(int horizontalMargin) {
		this.horizontalMargin = horizontalMargin;
	}

	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer renderer = mc.textRenderer;
		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - renderer.fontHeight / 2;
			case BOTTOM -> height - renderer.fontHeight;
			case TOP -> 0;
		};

		int textSpaceWidth = width - (horizontalMargin * 2);
		int textWidth = renderer.getWidth(text.asOrderedText());
		int xOffset = switch (horizontalAlignment) {
			case LEFT -> 0;
			case CENTER -> (textSpaceWidth - textWidth) / 2;
			case RIGHT -> textSpaceWidth - textWidth;
		};

		if (backgroundColor != null) {
			ScreenDrawing.coloredRect(matrices, x + xOffset - horizontalMargin, y, textWidth + (horizontalMargin * 2), height, this.backgroundColor);
		}

		if (textShadow) {
			renderer.drawWithShadow(matrices, text, x + xOffset, y + yOffset, LibGui.isDarkMode() ? darkmodeColor : color);
		} else {
			renderer.draw(matrices, text, x + xOffset, y + yOffset, LibGui.isDarkMode() ? darkmodeColor : color);
		}

		Style hoveredTextStyle = getTextStyleAt(mouseX, mouseY);
		ScreenDrawing.drawTextHover(matrices, hoveredTextStyle, x + mouseX, y + mouseY);
	}
}
