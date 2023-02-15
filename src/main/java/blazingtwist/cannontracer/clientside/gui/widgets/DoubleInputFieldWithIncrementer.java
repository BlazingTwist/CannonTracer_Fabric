package blazingtwist.cannontracer.clientside.gui.widgets;

import blazingtwist.cannontracer.shared.utils.StringUtils;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DoubleInputFieldWithIncrementer {

	public final BetterTextField field;
	public final TextureButton decrementButton;
	public final TextureButton incrementButton;

	public DoubleInputFieldWithIncrementer(WPlainPanel panel, int x, int y, int bgColor, Supplier<Double> supplier, Consumer<Double> consumer, Runnable onChange) {
		field = new BetterTextField();
		field.setTint(bgColor);
		field.setTextPredicate(StringUtils::isDouble);
		field.setOnSubmitListener(str -> {
			consumer.accept(StringUtils.parseDouble(str, 0d));
			if (onChange != null) {
				onChange.run();
			}
		});
		panel.add(field, x + 16, y, 64, 16);

		decrementButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		decrementButton.setLabelTexture(CTTextures.ICON_MINUS);
		decrementButton.setBackgroundColor(bgColor);
		decrementButton.setOnClick(() -> {
			double newValue = supplier.get() - 1d;
			consumer.accept(newValue);
			field.setText(Double.toString(newValue));
			if (onChange != null) {
				onChange.run();
			}
		});
		panel.add(decrementButton, x, y + 1, 16, 15);

		incrementButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		incrementButton.setLabelTexture(CTTextures.ICON_PLUS);
		incrementButton.setBackgroundColor(bgColor);
		incrementButton.setOnClick(() -> {
			double newValue = supplier.get() + 1d;
			consumer.accept(newValue);
			field.setText(Double.toString(newValue));
			if (onChange != null) {
				onChange.run();
			}
		});
		panel.add(incrementButton, x + 64 + 16, y + 1, 16, 15);
	}

	public int getWidth() {
		return 64 + 16 + 16;
	}
}
