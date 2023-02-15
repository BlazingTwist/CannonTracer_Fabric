package blazingtwist.cannontracer.mixin;

import blazingtwist.cannontracer.clientside.InputManager;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

	@Inject(at = @At("HEAD"), method = "onKey")
	public void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
		InputManager.getInstance().onKeyPressed(key, scancode, action, modifiers);
	}

}
