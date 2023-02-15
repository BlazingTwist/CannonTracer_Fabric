package blazingtwist.cannontracer.clientside.gui.widgets;

import io.github.cottonmc.cotton.gui.widget.data.Texture;
import java.util.function.Consumer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends TextureButton {

	private Text enabledText = Text.literal("true").formatted(Formatting.GREEN);
	private Text disabledText = Text.literal("false").formatted(Formatting.RED);
	private Texture enabledLabelTexture = null;
	private Texture disabledLabelTexture = null;
	private Integer enabledLabelColor = null;
	private Integer disabledLabelColor = null;

	private boolean state;
	private Consumer<Boolean> onToggleListener;

	public ToggleButton() {
		this(false);
	}

	public ToggleButton(boolean initialState) {
		this(CTTextures.BUTTON_PACKAGE_VANILLA_200, initialState);
	}

	public ToggleButton(String buttonPackage, boolean initialState) {
		super(buttonPackage);
		state = initialState;
		this.setOnClick(this::onToggle);
		updateLabels();
	}

	public ToggleButton setEnabledLabels(@Nullable Text text, @Nullable Texture texture, @Nullable Integer textureColor) {
		this.setEnabledText(text).setEnabledLabelTexture(texture).setEnabledLabelColor(textureColor);
		updateLabels();
		return this;
	}

	public ToggleButton setDisabledLabels(@Nullable Text text, @Nullable Texture texture, @Nullable Integer textureColor) {
		this.setDisabledText(text).setDisabledLabelTexture(texture).setDisabledLabelColor(textureColor);
		updateLabels();
		return this;
	}

	public ToggleButton setEnabledText(Text enabledText) {
		this.enabledText = enabledText;
		return this;
	}

	public ToggleButton setDisabledText(Text disabledText) {
		this.disabledText = disabledText;
		return this;
	}

	public ToggleButton setEnabledLabelTexture(Texture enabledLabelTexture) {
		this.enabledLabelTexture = enabledLabelTexture;
		return this;
	}

	public ToggleButton setDisabledLabelTexture(Texture disabledLabelTexture) {
		this.disabledLabelTexture = disabledLabelTexture;
		return this;
	}

	public ToggleButton setEnabledLabelColor(Integer enabledLabelColor) {
		this.enabledLabelColor = enabledLabelColor;
		return this;
	}

	public ToggleButton setDisabledLabelColor(Integer disabledLabelColor) {
		this.disabledLabelColor = disabledLabelColor;
		return this;
	}

	public void setState(boolean state) {
		this.state = state;
		updateLabels();
	}

	public boolean getState() {
		return state;
	}

	public void setOnToggleListener(Consumer<Boolean> onToggleListener) {
		this.onToggleListener = onToggleListener;
	}

	protected void onToggle() {
		setState(!state);
		if (onToggleListener != null) {
			onToggleListener.accept(state);
		}
	}

	private void updateLabels() {
		this.setLabelText(state ? enabledText : disabledText);
		this.setLabelTexture(state ? enabledLabelTexture : disabledLabelTexture);
		this.setLabelTextureColor(state ? enabledLabelColor : disabledLabelColor);
	}
}
