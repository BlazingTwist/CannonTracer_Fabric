package blazingtwist.cannontracer.clientside.gui;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon;
import net.minecraft.util.Identifier;

public interface Painters {

	BackgroundPainter VANILLA_DARK = BackgroundPainter.createNinePatch(new Identifier(LibGuiCommon.MOD_ID, "textures/widget/panel_dark.png"));

}
