package blazingtwist.cannontracer.clientside.gui.widgets;

import blazingtwist.cannontracer.shared.utils.StringUtils;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IntInputFieldWithIncrementer {

	public final BetterTextField field;
	public final TextureButton decrementButton;
	public final TextureButton incrementButton;
	public final int width;

	public IntInputFieldWithIncrementer(WPlainPanel panel, int x, int y, int fieldWidth, int bgColor,
										Supplier<Integer> supplier, Consumer<Integer> consumer, Runnable onChange) {
		this(panel, x, y, fieldWidth, bgColor, Integer.MIN_VALUE, Integer.MAX_VALUE, supplier, consumer, onChange);
	}

	public IntInputFieldWithIncrementer(WPlainPanel panel, int x, int y, int fieldWidth, int bgColor, int minValue, int maxValue,
										Supplier<Integer> supplier, Consumer<Integer> consumer, Runnable onChange) {
		field = new BetterTextField();
		field.setTint(bgColor);
		field.setTextPredicate(StringUtils::isInteger);
		field.setOnSubmitListener(str -> {
			int newValue = Math.min(maxValue, Math.max(minValue, StringUtils.parseInt(str, 0)));
			consumer.accept(newValue);
			if (onChange != null) {
				onChange.run();
			}
		});
		panel.add(field, x + 16, y, fieldWidth, 16);

		decrementButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		decrementButton.setLabelTexture(CTTextures.ICON_MINUS);
		decrementButton.setBackgroundColor(bgColor);
		decrementButton.setOnClick(() -> {
			int newValue = Math.min(maxValue, Math.max(minValue, supplier.get() - 1));
			consumer.accept(newValue);
			field.setText(Integer.toString(newValue));
			if (onChange != null) {
				onChange.run();
			}
		});
		panel.add(decrementButton, x, y + 1, 16, 15);

		incrementButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		incrementButton.setLabelTexture(CTTextures.ICON_PLUS);
		incrementButton.setBackgroundColor(bgColor);
		incrementButton.setOnClick(() -> {
			int newValue = Math.min(maxValue, Math.max(minValue, supplier.get() + 1));
			consumer.accept(newValue);
			field.setText(Integer.toString(newValue));
			if (onChange != null) {
				onChange.run();
			}
		});
		panel.add(incrementButton, x + fieldWidth + 16, y + 1, 16, 15);

		this.width = fieldWidth + 16 + 16;
	}
}
