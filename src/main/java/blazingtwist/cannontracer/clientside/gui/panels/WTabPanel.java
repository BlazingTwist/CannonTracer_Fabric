package blazingtwist.cannontracer.clientside.gui.panels;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon;
import io.github.cottonmc.cotton.gui.impl.client.NarrationMessages;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WCardPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class WTabPanel extends WPanel {
	private static final int TAB_PADDING = 4;
	private static final int TAB_WIDTH = 28;
	private static final int TAB_HEIGHT = 30;
	private static final int ICON_SIZE = 16;
	private final WBox tabRibbon = new WBox(Axis.HORIZONTAL).setSpacing(1);
	private final List<WTab> tabWidgets = new ArrayList<>();
	private final Map<Tab, WTab> tabWidgetsByData = new HashMap<>();
	private final WCardPanel mainPanel = new WCardPanel();

	public WTabPanel() {
		add(tabRibbon, 0, 0);
		add(mainPanel, 0, TAB_HEIGHT);
	}

	private void add(WWidget widget, int x, int y) {
		children.add(widget);
		widget.setParent(this);
		widget.setLocation(x, y);
		expandToFit(widget);
	}

	public void add(Tab tab) {
		WTab tabWidget = new WTab(tab);

		if (tabWidgets.isEmpty()) {
			tabWidget.selected = true;
		}

		tabWidgets.add(tabWidget);
		tabWidgetsByData.put(tab, tabWidget);
		tabRibbon.add(tabWidget, TAB_WIDTH, TAB_HEIGHT + TAB_PADDING);
		mainPanel.add(tab.getWidget());
	}

	public void add(WWidget widget, Consumer<Tab.Builder> configurator) {
		Tab.Builder builder = new Tab.Builder(widget);
		configurator.accept(builder);
		add(builder.build());
	}

	public Tab getSelectedTab() {
		return ((WTab) mainPanel.getSelectedCard()).data;
	}

	public WTabPanel setSelectedTab(Tab tab) {
		Objects.requireNonNull(tab, "tab");
		WTab widget = tabWidgetsByData.get(tab);

		if (widget == null) {
			throw new NoSuchElementException("Trying to select unknown tab " + tab);
		}

		return setSelectedIndex(tabWidgets.indexOf(widget));
	}

	public int getSelectedIndex() {
		return mainPanel.getSelectedIndex();
	}

	public WTabPanel setSelectedIndex(int tabIndex) {
		mainPanel.setSelectedIndex(tabIndex);

		for (int i = 0; i < getTabCount(); i++) {
			tabWidgets.get(i).selected = (i == tabIndex);
		}

		layout();
		return this;
	}

	public int getTabCount() {
		return tabWidgets.size();
	}

	@Override
	public void setSize(int x, int y) {
		super.setSize(x, y);
		tabRibbon.setSize(x, TAB_HEIGHT);
	}

	@Override
	public void addPainters() {
		super.addPainters();
		mainPanel.setBackgroundPainter(blazingtwist.cannontracer.clientside.gui.Painters.VANILLA_DARK);
	}

	public static class Tab {
		@Nullable
		private final Text title;
		@Nullable
		private final Icon icon;
		private final WWidget widget;
		@Nullable
		private final Consumer<TooltipBuilder> tooltip;

		private Tab(@Nullable Text title, @Nullable Icon icon, WWidget widget, @Nullable Consumer<TooltipBuilder> tooltip) {
			if (title == null && icon == null) {
				throw new IllegalArgumentException("A tab must have a title or an icon");
			}

			this.title = title;
			this.icon = icon;
			this.widget = Objects.requireNonNull(widget, "widget");
			this.tooltip = tooltip;
		}

		@Nullable
		public Text getTitle() {
			return title;
		}

		@Nullable
		public Icon getIcon() {
			return icon;
		}

		public WWidget getWidget() {
			return widget;
		}

		public void addTooltip(TooltipBuilder tooltip) {
			if (this.tooltip != null) {
				this.tooltip.accept(tooltip);
			}
		}

		public static final class Builder {
			@Nullable
			private Text title;
			@Nullable
			private Icon icon;
			private final WWidget widget;
			private final List<Text> tooltip = new ArrayList<>();

			public Builder(WWidget widget) {
				this.widget = Objects.requireNonNull(widget, "widget");
			}

			public Builder title(Text title) {
				this.title = Objects.requireNonNull(title, "title");
				return this;
			}

			public Builder icon(Icon icon) {
				this.icon = Objects.requireNonNull(icon, "icon");
				return this;
			}

			public Builder tooltip(Text... lines) {
				Objects.requireNonNull(lines, "lines");
				Collections.addAll(tooltip, lines);

				return this;
			}

			public Builder tooltip(Collection<? extends Text> lines) {
				Objects.requireNonNull(lines, "lines");
				tooltip.addAll(lines);
				return this;
			}

			public Tab build() {
				Consumer<TooltipBuilder> tooltip = null;

				if (!this.tooltip.isEmpty()) {
					//noinspection Convert2Lambda
					tooltip = new Consumer<TooltipBuilder>() {
						@Override
						public void accept(TooltipBuilder builder) {
							builder.add(Tab.Builder.this.tooltip.toArray(new Text[0]));
						}
					};
				}

				return new Tab(title, icon, widget, tooltip);
			}
		}
	}

	private final class WTab extends WWidget {
		private final Tab data;
		boolean selected = false;

		WTab(Tab data) {
			this.data = data;
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
		public InputResult onClick(int x, int y, int button) {
			super.onClick(x, y, button);

			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

			setSelectedIndex(tabWidgets.indexOf(this));
			return InputResult.PROCESSED;
		}

		@Override
		public void onKeyPressed(int ch, int key, int modifiers) {
			if (isActivationKey(ch)) {
				onClick(0, 0, 0);
			}
		}

		@Override
		public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
			TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
			Text title = data.getTitle();
			Icon icon = data.getIcon();

			if (title != null) {
				int width = TAB_WIDTH + renderer.getWidth(title);
				if (icon == null) width = Math.max(TAB_WIDTH, width - ICON_SIZE);

				if (this.width != width) {
					setSize(width, this.height);
					getParent().layout();
				}
			}

			(selected ? Painters.SELECTED_TAB : Painters.UNSELECTED_TAB).paintBackground(matrices, x, y, this);
			if (isFocused()) {
				(selected ? Painters.SELECTED_TAB_FOCUS_BORDER : Painters.UNSELECTED_TAB_FOCUS_BORDER).paintBackground(matrices, x, y, this);
			}

			int iconX = 6;

			if (title != null) {
				int titleX = (icon != null) ? iconX + ICON_SIZE + 1 : 0;
				int titleY = (height - TAB_PADDING - renderer.fontHeight) / 2 + 1;
				int width = (icon != null) ? this.width - iconX - ICON_SIZE : this.width;
				HorizontalAlignment align = (icon != null) ? HorizontalAlignment.LEFT : HorizontalAlignment.CENTER;

				int color;
				if (LibGui.isDarkMode()) {
					color = WLabel.DEFAULT_DARKMODE_TEXT_COLOR;
				} else {
					color = selected ? WLabel.DEFAULT_TEXT_COLOR : 0xEEEEEE;
				}

				ScreenDrawing.drawString(matrices, title.asOrderedText(), align, x + titleX, y + titleY, width, color);
			}

			if (icon != null) {
				icon.paint(matrices, x + iconX, y + (height - TAB_PADDING - ICON_SIZE) / 2, ICON_SIZE);
			}
		}

		@Override
		public void addTooltip(TooltipBuilder tooltip) {
			data.addTooltip(tooltip);
		}

		@Override
		public void addNarrations(NarrationMessageBuilder builder) {
			Text label = data.getTitle();

			if (label != null) {
				builder.put(NarrationPart.TITLE, Text.translatable(NarrationMessages.TAB_TITLE_KEY, label));
			}

			builder.put(NarrationPart.POSITION, Text.translatable(NarrationMessages.TAB_POSITION_KEY, tabWidgets.indexOf(this) + 1, tabWidgets.size()));
		}
	}

	/**
	 * Internal background painter instances for tabs.
	 */
	final static class Painters {
		static final BackgroundPainter SELECTED_TAB = BackgroundPainter
				.createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/tab/selected_dark.png")).setTopPadding(2);

		static final BackgroundPainter UNSELECTED_TAB = BackgroundPainter
				.createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/tab/unselected_dark.png"));

		static final BackgroundPainter SELECTED_TAB_FOCUS_BORDER = BackgroundPainter
				.createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/tab/focus.png")).setTopPadding(2);

		static final BackgroundPainter UNSELECTED_TAB_FOCUS_BORDER = BackgroundPainter
				.createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/tab/focus.png"));
	}
}
