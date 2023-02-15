package blazingtwist.cannontracer.clientside.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import java.util.function.BiConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class KeyBindField extends WWidget {

	private static final int BACKGROUND_COLOR = 0xFF707070;
	private static final int BORDER_COLOR_LOWLIGHT = 0xFF333337;
	private static final int BORDER_COLOR_HIGHLIGHT = 0xFFE3E3E4;
	private static final int SELECTED_COLOR_LOWLIGHT = 0xFFA3A337;
	private static final int SELECTED_COLOR_HIGHLIGHT = 0xFFFFFFB4;

	private InputUtil.Key boundKey;
	private BiConsumer<InputUtil.Key, InputUtil.Key> onKeyChanged;

	public InputUtil.Key getBoundKey() {
		return boundKey;
	}

	public KeyBindField setBoundKey(InputUtil.Key boundKey) {
		this.boundKey = boundKey;
		return this;
	}

	public void setOnKeyChanged(BiConsumer<InputUtil.Key, InputUtil.Key> listener) {
		this.onKeyChanged = listener;
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
		this.drawBox(matrices, x, y);
		this.drawText(matrices, x, y);
	}

	protected void drawBox(MatrixStack matrices, int x, int y) {
		int borderLowLight = isFocused() ? SELECTED_COLOR_LOWLIGHT : BORDER_COLOR_LOWLIGHT;
		int borderHighLight = isFocused() ? SELECTED_COLOR_HIGHLIGHT : BORDER_COLOR_HIGHLIGHT;

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		ScreenDrawing.coloredRect(matrices, x, y, width, height, borderHighLight);
		ScreenDrawing.coloredRect(matrices, x, y, width - 1, height - 1, borderLowLight);
		ScreenDrawing.coloredRect(matrices, x + 1, y + 1, width - 2, height - 2, BACKGROUND_COLOR);
	}

	protected void drawText(MatrixStack matrices, int x, int y) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Text displayText = boundKey == null ? Text.literal("none") : boundKey.getLocalizedText();
		float xOffset = (this.width - textRenderer.getWidth(displayText)) / 2f;
		float yOffset = (this.height - 8) / 2f;
		textRenderer.drawWithShadow(matrices, displayText, x + xOffset, y + yOffset, 0xE0E0E0);
	}

	@Override
	public InputResult onClick(int x, int y, int button) {
		this.requestFocus();
		return InputResult.PROCESSED;
	}

	@Override
	public void onKeyPressed(int ch, int key, int modifiers) {
		InputUtil.Key previousKey = boundKey;
		boundKey = InputUtil.Type.KEYSYM.createFromCode(ch);
		if (onKeyChanged != null) {
			onKeyChanged.accept(previousKey, boundKey);
		}
		MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		this.releaseFocus();
	}
}
