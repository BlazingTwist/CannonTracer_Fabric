package blazingtwist.cannontracer.clientside.gui.widgets;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.impl.client.NarrationMessages;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.Texture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TextureButton extends WWidget {
	private final Texture normalTexture;
	private final Texture hoverTexture;
	private final Texture pressedTexture;
	private int backgroundColor = 0xFF_FFFFFF;

	@Nullable
	private Text labelText;
	private HorizontalAlignment labelAlignment = HorizontalAlignment.CENTER;
	@Nullable
	private Texture labelTexture;
	@Nullable
	private Integer labelTextureColor;
	@Nullable
	private Runnable onClick;

	private int pressedYOffset = 1;

	private boolean isMouseDown;

	public TextureButton(Texture normalTexture, Texture hoverTexture, Texture pressedTexture) {
		this.normalTexture = normalTexture;
		this.hoverTexture = hoverTexture;
		this.pressedTexture = pressedTexture;
	}

	public TextureButton(String buttonPackage) {
		this.normalTexture = new Texture(new Identifier(buttonPackage + "/normal.png"));
		this.hoverTexture = new Texture(new Identifier(buttonPackage + "/hover.png"));
		this.pressedTexture = new Texture(new Identifier(buttonPackage + "/pressed.png"));
	}

	public TextureButton(String namespace, String buttonDirectory) {
		this.normalTexture = new Texture(new Identifier(namespace, buttonDirectory + "/normal.png"));
		this.hoverTexture = new Texture(new Identifier(namespace, buttonDirectory + "/hover.png"));
		this.pressedTexture = new Texture(new Identifier(namespace, buttonDirectory + "/pressed.png"));
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setLabelText(@Nullable Text labelText) {
		this.labelText = labelText;
	}

	public void setLabelAlignment(HorizontalAlignment labelAlignment) {
		this.labelAlignment = labelAlignment;
	}

	public void setLabelTexture(@Nullable Texture labelTexture) {
		this.labelTexture = labelTexture;
	}

	public void setLabelTextureColor(@Nullable Integer labelColor) {
		this.labelTextureColor = labelColor;
	}

	public void setOnClick(@Nullable Runnable onClick) {
		this.onClick = onClick;
	}

	public void setPressedYOffset(int pressedYOffset) {
		this.pressedYOffset = pressedYOffset;
	}

	@Override
	public boolean canResize() {
		return true;
	}

	@Override
	public boolean canFocus() {
		return true;
	}

	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		boolean hovered = isFocused() || (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
		Texture stateTexture = isMouseDown ? pressedTexture : (hovered ? hoverTexture : normalTexture);
		int yOffset = isMouseDown ? pressedYOffset : 0;
		ScreenDrawing.texturedRect(matrices, x, y + yOffset, getWidth(), getHeight(), stateTexture, backgroundColor);

		if (labelTexture != null) {
			int textureColor = labelTextureColor != null ? labelTextureColor : 0xFFFFFF;
			ScreenDrawing.texturedRect(matrices, x, y + yOffset, getWidth(), getHeight(), labelTexture, textureColor);
		}

		if (labelText != null) {
			ScreenDrawing.drawStringWithShadow(matrices, labelText.asOrderedText(), labelAlignment, x, y + yOffset + (height - 8) / 2, width, 0xE0E0E0);
		}

		super.paint(matrices, x, y, mouseX, mouseY);
	}

	@Override
	public InputResult onMouseDown(int x, int y, int button) {
		super.onMouseDown(x, y, button);

		if (isWithinBounds(x, y)) {
			isMouseDown = true;
			return InputResult.PROCESSED;
		}
		return InputResult.IGNORED;
	}

	@Override
	public InputResult onMouseUp(int x, int y, int button) {
		super.onMouseUp(x, y, button);
		isMouseDown = false;
		return isWithinBounds(x, y) ? InputResult.PROCESSED : InputResult.IGNORED;
	}

	@Override
	public InputResult onClick(int x, int y, int button) {
		super.onClick(x, y, button);

		if (isWithinBounds(x, y)) {
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

			if (onClick != null) onClick.run();
			return InputResult.PROCESSED;
		}

		return InputResult.IGNORED;
	}

	@Override
	public void onKeyPressed(int ch, int key, int modifiers) {
		if (isActivationKey(ch)) {
			onClick(0, 0, 0);
		}
	}

	@Override
	public void addNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, ClickableWidget.getNarrationMessage(labelText));

		if (isFocused()) {
			builder.put(NarrationPart.USAGE, NarrationMessages.Vanilla.BUTTON_USAGE_FOCUSED);
		} else if (isHovered()) {
			builder.put(NarrationPart.USAGE, NarrationMessages.Vanilla.BUTTON_USAGE_HOVERED);
		}
	}
}
