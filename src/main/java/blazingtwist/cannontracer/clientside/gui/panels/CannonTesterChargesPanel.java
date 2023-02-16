package blazingtwist.cannontracer.clientside.gui.panels;

import blazingtwist.cannontracer.clientside.gui.widgets.BetterTextField;
import blazingtwist.cannontracer.clientside.gui.widgets.CTTextures;
import blazingtwist.cannontracer.clientside.gui.widgets.IntInputFieldWithIncrementer;
import blazingtwist.cannontracer.clientside.gui.widgets.LabelWithShadow;
import blazingtwist.cannontracer.clientside.gui.widgets.TextureButton;
import blazingtwist.cannontracer.clientside.gui.widgets.ToggleButton;
import blazingtwist.cannontracer.shared.datatypes.TestCannonData;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollPanel;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CannonTesterChargesPanel extends WPlainPanel {

	public static CannonTesterChargesPanel addPanel(WPlainPanel parent, int x, int y, int height) {
		CannonTesterChargesPanel panel = new CannonTesterChargesPanel(height);
		parent.add(panel, x, y);
		return panel;
	}

	private static final int xGap = 6;

	private final ChargesPanel chargesPanel;

	private TestCannonData cannon;

	private CannonTesterChargesPanel(int height) {
		this.setInsets(Insets.NONE);
		HeaderRow headerRow = new HeaderRow(this);
		this.add(headerRow, 6, 0);

		chargesPanel = new ChargesPanel(this, headerRow.getWidth(), height - 20);
		WScrollPanel scrollPanel = new WScrollPanel(chargesPanel);
		chargesPanel.scrollParent = scrollPanel;
		this.add(scrollPanel, 6, 16 + 4, headerRow.getWidth() + 12, height - 20);
		this.setSize(headerRow.getWidth(), height);
	}

	@Override
	public boolean canResize() {
		return false;
	}

	public void load(TestCannonData cannon) {
		this.cannon = cannon;
		chargesPanel.load(cannon);
	}

	public void createCharge() {
		chargesPanel.createCharge();
	}

	private static class HeaderRow extends WPlainPanel {
		private final CannonTesterChargesPanel parent;

		public HeaderRow(CannonTesterChargesPanel parent) {
			this.setInsets(Insets.NONE);
			this.parent = parent;

			int x = 0;
			x += addPlusButton(x) + xGap;
			x += addLabel("gui.cannontester.editor_tab.charge_enabled", x, 48) + xGap;
			x += addLabel("gui.cannontester.editor_tab.charge_amount", x, 48 + 16 + 16) + xGap;
			x += addLabel("gui.cannontester.editor_tab.charge_delay", x, 48 + 16 + 16) + xGap;
			x += addLabel("gui.cannontester.editor_tab.charge_random", x, 48) + xGap;
			addLabel("gui.cannontester.editor_tab.charge_note", x, 160);

			this.setSize(x + 160, 16);
		}

		@Override
		public boolean canResize() {
			return false;
		}

		private int addPlusButton(int x) {
			TextureButton button = new TextureButton(CTTextures.BUTTON_PACKAGE_GREEN_CIRCLE);
			button.setLabelTexture(CTTextures.ICON_PLUS);
			button.setOnClick(this::onAddChargePressed);
			this.add(button, x, 2, 11, 11);
			return 11;
		}

		private int addLabel(String translationKey, int x, int width) {
			LabelWithShadow label = new LabelWithShadow(Text.translatable(translationKey).formatted(Formatting.WHITE));
			label.setTextShadow(false);
			label.setHorizontalAlignment(HorizontalAlignment.CENTER);
			label.setVerticalAlignment(VerticalAlignment.CENTER);
			this.add(label, x, 0, width, 16);
			return width;
		}

		private void onAddChargePressed() {
			parent.createCharge();
		}
	}

	private static class ChargesPanel extends WBox {
		private final CannonTesterChargesPanel parent;
		private WScrollPanel scrollParent;

		public ChargesPanel(CannonTesterChargesPanel parent, int width, int height) {
			super(Axis.VERTICAL);
			this.parent = parent;
			this.setSize(width, height);
		}

		@Override
		public boolean canResize() {
			return false;
		}

		public void load(TestCannonData cannon) {
			this.children.clear();
			this.height = 0;

			for (TestCannonData.CannonCharge charge : cannon.getCharges()) {
				ChargeRow row = new ChargeRow(this, charge);
				this.add(row);
				row.setHost(this.getHost());
			}
			forceLayout();
		}

		public void createCharge() {
			TestCannonData.CannonCharge charge = new TestCannonData.CannonCharge(true, 0, 0, false, "");
			ChargeRow row = new ChargeRow(this, charge);
			this.add(row);
			row.setHost(this.getHost());
			forceLayout();

			parent.cannon.getCharges().add(charge);
		}

		public void deleteCharge(ChargeRow row) {
			this.height -= (row.getHeight() + this.spacing);
			this.remove(row);
			forceLayout();

			parent.cannon.getCharges().remove(row.charge);
		}

		public void forceLayout() {
			this.layout();
			if (scrollParent != null) {
				scrollParent.layout();
			}
		}
	}

	private static class ChargeRow extends WPlainPanel {
		private final ChargesPanel parent;
		private final TestCannonData.CannonCharge charge;

		public ChargeRow(ChargesPanel parent, TestCannonData.CannonCharge charge) {
			this.setInsets(Insets.NONE);
			this.parent = parent;
			this.charge = charge;

			int x = 0;
			x += addDeleteButton(x) + xGap;
			x += addEnabledToggle(x) + xGap;
			x += addIntegerInput(x, charge::getAmount, charge::setAmount) + xGap;
			x += addIntegerInput(x, charge::getDelay, charge::setDelay) + xGap;
			x += addRandomToggle(x) + xGap;
			addNoteInput(x);
		}

		private int addDeleteButton(int x) {
			TextureButton button = new TextureButton(CTTextures.BUTTON_PACKAGE_RED_CIRCLE);
			button.setLabelTexture(CTTextures.ICON_CROSS);
			button.setOnClick(this::onDeletePressed);
			this.add(button, x, 2, 11, 11);
			return 11;
		}

		private int addEnabledToggle(int x) {
			ToggleButton button = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, charge.isEnabled());
			button.setOnToggleListener(charge::setEnabled);
			this.add(button, x, 0, 48, 16);
			return 48;
		}

		private int addIntegerInput(int x, Supplier<Integer> supplier, Consumer<Integer> consumer) {
			IntInputFieldWithIncrementer intField = new IntInputFieldWithIncrementer(this, x, 0, 48, 0xFF_FFFFFF,
					supplier, consumer, null);
			intField.field.setText(Integer.toString(supplier.get()));
			return intField.width;
		}

		private int addRandomToggle(int x) {
			ToggleButton button = new ToggleButton(CTTextures.BUTTON_PACKAGE_VANILLA_50, charge.getRandom());
			button.setOnToggleListener(charge::setRandom);
			this.add(button, x, 0, 48, 16);
			return 48;
		}

		private void addNoteInput(int x) {
			BetterTextField field = new BetterTextField();
			field.setMaxLength(255);
			field.setText(charge.getNote());
			field.setOnSubmitListener(charge::setNote);
			this.add(field, x, 0, 160, 16);
		}

		private void onDeletePressed() {
			parent.deleteCharge(this);
		}
	}
}
