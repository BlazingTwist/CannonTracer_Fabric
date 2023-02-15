package blazingtwist.cannontracer.clientside.gui.panels;

import blazingtwist.cannontracer.clientside.gui.Painters;
import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.clientside.gui.widgets.ColoredBoxWidget;
import blazingtwist.cannontracer.clientside.gui.widgets.DoubleInputFieldWithIncrementer;
import blazingtwist.cannontracer.clientside.gui.widgets.LabelWithShadow;
import blazingtwist.cannontracer.clientside.gui.widgets.TextureButton;
import blazingtwist.cannontracer.shared.datatypes.MutableVec3d;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import blazingtwist.cannontracer.shared.datatypes.TntAlignment;
import blazingtwist.cannontracer.shared.utils.ColorUtils;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;

public class CannonTesterPanel extends WPlainPanel {

	public static CannonTesterPanel createPanel() {
		return new CannonTesterPanel();
	}

	private final List<Runnable> onLoadListeners = new ArrayList<>();
	private final LabelWithShadow worldPositionX;
	private final LabelWithShadow worldPositionY;
	private final LabelWithShadow worldPositionZ;
	private final CannonTesterChargesPanel chargesPanel;

	private Vec3i commandPos;
	private TestCannonData cannon;
	private final MutableVec3d blockOffset = new MutableVec3d();
	private final MutableVec3d pixelOffset = new MutableVec3d();
	private final MutableVec3d velocity = new MutableVec3d();
	private final MutableTntAlign align = new MutableTntAlign();

	private CannonTesterPanel() {
		this.setBackgroundPainter(Painters.VANILLA_DARK);
		this.setInsets(new Insets(10));
		this.setSize(500, 280);

		final int uiPosY_xData = 16;
		final int uiPosY_yData = 36;
		final int uiPosY_zData = 56;

		final int redTint = 0xff_ffcccc;
		final int greenTint = 0xff_ccffcc;
		final int blueTint = 0xff_ccccff;
		final int neutralTint = 0xff_ffffff;

		worldPositionX = new LabelWithShadow(Text.literal("0.0"));
		worldPositionY = new LabelWithShadow(Text.literal("0.0"));
		worldPositionZ = new LabelWithShadow(Text.literal("0.0"));

		addCenteredLabel(Text.literal("X"), 0, uiPosY_xData, 16, redTint);
		addCenteredLabel(Text.literal("Y"), 0, uiPosY_yData, 16, greenTint);
		addCenteredLabel(Text.literal("Z"), 0, uiPosY_zData, 16, blueTint);

		final int uiPos_blockX = 16;
		addCenteredLabel("gui.cannontester.editor_tab.block_offset", uiPos_blockX, 0, 64 + 16 + 16, neutralTint);
		addDoubleInputWithIncrementButtons(uiPos_blockX, uiPosY_xData, redTint, blockOffset::getX, blockOffset::setX, this::onUpdateXPosition);
		addDoubleInputWithIncrementButtons(uiPos_blockX, uiPosY_yData, greenTint, blockOffset::getY, blockOffset::setY, this::onUpdateYPosition);
		addDoubleInputWithIncrementButtons(uiPos_blockX, uiPosY_zData, blueTint, blockOffset::getZ, blockOffset::setZ, this::onUpdateZPosition);

		final int uiPos_pixelX = uiPos_blockX + 64 + 16 + 16 + 8;
		addCenteredLabel("gui.cannontester.editor_tab.pixel_offset", uiPos_pixelX, 0, 64 + 16 + 16, neutralTint);
		addDoubleInputWithIncrementButtons(uiPos_pixelX, uiPosY_xData, redTint, pixelOffset::getX, pixelOffset::setX, this::onUpdateXPosition);
		addDoubleInputWithIncrementButtons(uiPos_pixelX, uiPosY_yData, greenTint, pixelOffset::getY, pixelOffset::setY, this::onUpdateYPosition);
		addDoubleInputWithIncrementButtons(uiPos_pixelX, uiPosY_zData, blueTint, pixelOffset::getZ, pixelOffset::setZ, this::onUpdateZPosition);

		final int uiPos_alignX = uiPos_pixelX + 64 + 16 + 16 + 8;
		addCenteredLabel("gui.cannontester.editor_tab.align", uiPos_alignX, 0, 48, neutralTint);
		addAlignButtons(uiPos_alignX, uiPosY_xData, redTint, align::getX, align::setX, this::onUpdateXPosition);
		addAlignButtons(uiPos_alignX, uiPosY_yData, greenTint, align::getY, align::setY, this::onUpdateYPosition);
		addAlignButtons(uiPos_alignX, uiPosY_zData, blueTint, align::getZ, align::setZ, this::onUpdateZPosition);

		final int uiPos_equalsSign = uiPos_alignX + 48;
		this.addCenteredLabel(Text.literal("="), uiPos_equalsSign, uiPosY_xData, 16, redTint);
		this.addCenteredLabel(Text.literal("="), uiPos_equalsSign, uiPosY_yData, 16, greenTint);
		this.addCenteredLabel(Text.literal("="), uiPos_equalsSign, uiPosY_zData, 16, blueTint);

		final int uiPos_worldPosText = uiPos_equalsSign + 16;
		addCenteredLabel("gui.cannontester.editor_tab.world_position", uiPos_worldPosText, 0, 64, neutralTint);
		addWorldPosText(worldPositionX, uiPos_worldPosText, uiPosY_xData, 64, redTint);
		addWorldPosText(worldPositionY, uiPos_worldPosText, uiPosY_yData, 64, greenTint);
		addWorldPosText(worldPositionZ, uiPos_worldPosText, uiPosY_zData, 64, blueTint);

		final int uiPos_velocity = uiPos_worldPosText + 64 + 24;
		addCenteredLabel("gui.cannontester.editor_tab.velocity", uiPos_velocity, 0, 64 + 16 + 16, neutralTint);
		addDoubleInputWithIncrementButtons(uiPos_velocity, uiPosY_xData, redTint, velocity::getX, velocity::setX, null);
		addDoubleInputWithIncrementButtons(uiPos_velocity, uiPosY_yData, greenTint, velocity::getY, velocity::setY, null);
		addDoubleInputWithIncrementButtons(uiPos_velocity, uiPosY_zData, blueTint, velocity::getZ, velocity::setZ, null);

		chargesPanel = CannonTesterChargesPanel.addPanel(this, 0, uiPosY_zData + 16 + 4 + 16, 168);
	}

	public void loadCannon(Vec3i commandPos, TestCannonData cannon) {
		this.commandPos = commandPos;
		this.cannon = cannon;
		blockOffset.set(cannon.getBlockOffset());
		pixelOffset.set(cannon.getPixelOffset());
		velocity.set(cannon.getVelocity());
		align.load(cannon);
		chargesPanel.load(cannon);

		for (Runnable listener : onLoadListeners) {
			listener.run();
		}
		this.onUpdateXPosition();
		this.onUpdateYPosition();
		this.onUpdateZPosition();
	}

	public Vec3i getCommandPos() {
		return commandPos;
	}

	public TestCannonData buildCannon() {
		cannon.setBlockOffset(blockOffset.toVec3d());
		cannon.setPixelOffset(pixelOffset.toVec3d());
		cannon.setVelocity(velocity.toVec3d());
		cannon.setXAlign(align.x);
		cannon.setYAlign(align.y);
		cannon.setZAlign(align.z);
		return cannon;
	}

	private void addCenteredLabel(String translationKey, int x, int y, int width, int textColor) {
		addCenteredLabel(Text.translatable(translationKey), x, y, width, textColor);
	}

	private void addCenteredLabel(Text text, int x, int y, int width, int textColor) {
		LabelWithShadow label = new LabelWithShadow(text);
		label.setTextShadow(false);
		label.setHorizontalAlignment(HorizontalAlignment.CENTER);
		label.setVerticalAlignment(VerticalAlignment.CENTER);
		label.setColor(textColor);
		this.add(label, x, y, width, 16);
	}

	private void addDoubleInputWithIncrementButtons(int x, int y, int bgColor, Supplier<Double> supplier, Consumer<Double> consumer, Runnable onChange) {
		DoubleInputFieldWithIncrementer doubleField = new DoubleInputFieldWithIncrementer(this, x, y, bgColor, supplier, consumer, onChange);
		onLoadListeners.add(() -> doubleField.field.setText(Double.toString(supplier.get())));
	}

	private void addAlignButtons(int x, int y, int bgColor, Supplier<TntAlignment> supplier, Consumer<TntAlignment> consumer, Runnable onChange) {
		final int enabledColor = 0xFF_55FF55;
		final int disabledColor = 0xFF_555555;

		TextureButton leftButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		leftButton.setLabelTexture(CTTextures.ICON_ALIGN_LEFT);
		leftButton.setBackgroundColor(bgColor);

		TextureButton centerButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		centerButton.setLabelTexture(CTTextures.ICON_ALIGN_CENTER);
		centerButton.setBackgroundColor(bgColor);

		TextureButton rightButton = new TextureButton(CTTextures.BUTTON_PACKAGE_VANILLA_50);
		rightButton.setLabelTexture(CTTextures.ICON_ALIGN_RIGHT);
		rightButton.setBackgroundColor(bgColor);

		leftButton.setOnClick(() -> {
			leftButton.setLabelTextureColor(enabledColor);
			centerButton.setLabelTextureColor(disabledColor);
			rightButton.setLabelTextureColor(disabledColor);
			consumer.accept(TntAlignment.MINUS_ONE);
			onChange.run();
		});

		centerButton.setOnClick(() -> {
			leftButton.setLabelTextureColor(disabledColor);
			centerButton.setLabelTextureColor(enabledColor);
			rightButton.setLabelTextureColor(disabledColor);
			consumer.accept(TntAlignment.NONE);
			onChange.run();
		});

		rightButton.setOnClick(() -> {
			leftButton.setLabelTextureColor(disabledColor);
			centerButton.setLabelTextureColor(disabledColor);
			rightButton.setLabelTextureColor(enabledColor);
			consumer.accept(TntAlignment.PLUS_ONE);
			onChange.run();
		});

		this.add(leftButton, x, y, 16, 16);
		this.add(centerButton, x + 16, y, 16, 16);
		this.add(rightButton, x + 32, y, 16, 16);

		onLoadListeners.add(() -> {
			TntAlignment currentAlignment = supplier.get();
			leftButton.setLabelTextureColor(currentAlignment == TntAlignment.MINUS_ONE ? enabledColor : disabledColor);
			centerButton.setLabelTextureColor(currentAlignment == TntAlignment.NONE ? enabledColor : disabledColor);
			rightButton.setLabelTextureColor(currentAlignment == TntAlignment.PLUS_ONE ? enabledColor : disabledColor);
		});
	}

	private void addWorldPosText(LabelWithShadow widget, int x, int y, int width, int bgColor) {
		ColoredBoxWidget coloredBox = new ColoredBoxWidget();
		coloredBox.setColor(ColorUtils.multiply(0xFF_666666, bgColor));
		coloredBox.setBorderColor(ColorUtils.multiply(0xFF_2b2b31, bgColor));
		coloredBox.setBorderWidth(1);
		this.add(coloredBox, x, y, width, 16);

		widget.setTextShadow(false);
		widget.setHorizontalAlignment(HorizontalAlignment.CENTER);
		widget.setVerticalAlignment(VerticalAlignment.CENTER);
		widget.setColor(0xFF_AAAAAA);
		this.add(widget, x, y, width, 16);
	}

	private void onUpdateXPosition() {
		double x = commandPos.getX() + 0.5d + blockOffset.getX() + (pixelOffset.getX() / 16d) + align.x.getBlockOffset();
		worldPositionX.setText(Text.literal(Double.toString(x)));
	}

	private void onUpdateYPosition() {
		double y = commandPos.getY() + 0.01d + blockOffset.getY() + (pixelOffset.getY() / 16d) + align.y.getBlockOffset();
		worldPositionY.setText(Text.literal(Double.toString(y)));
	}

	private void onUpdateZPosition() {
		double z = commandPos.getZ() + 0.5d + blockOffset.getZ() + (pixelOffset.getZ() / 16d) + align.z.getBlockOffset();
		worldPositionZ.setText(Text.literal(Double.toString(z)));
	}

	private static class MutableTntAlign {
		public TntAlignment x;
		public TntAlignment y;
		public TntAlignment z;

		public MutableTntAlign() {
		}

		public void load(TestCannonData cannon) {
			this.x = cannon.getXAlign();
			this.y = cannon.getYAlign();
			this.z = cannon.getZAlign();
		}

		public void writeTo(TestCannonData cannon) {
			cannon.setXAlign(x);
			cannon.setYAlign(y);
			cannon.setZAlign(z);
		}

		public TntAlignment getX() {
			return x;
		}

		public void setX(TntAlignment x) {
			this.x = x;
		}

		public TntAlignment getY() {
			return y;
		}

		public void setY(TntAlignment y) {
			this.y = y;
		}

		public TntAlignment getZ() {
			return z;
		}

		public void setZ(TntAlignment z) {
			this.z = z;
		}
	}

}
