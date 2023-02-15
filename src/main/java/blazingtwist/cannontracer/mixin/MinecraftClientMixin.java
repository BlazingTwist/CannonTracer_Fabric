package blazingtwist.cannontracer.mixin;

import blazingtwist.cannontracer.clientside.SettingsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@Inject(at = @At("TAIL"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
	public void onDisconnect(Screen screen, CallbackInfo info) {
		SettingsManager.getInstance().onQuitFromServer();
	}

}
