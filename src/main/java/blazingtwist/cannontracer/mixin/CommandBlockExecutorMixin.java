package blazingtwist.cannontracer.mixin;

import blazingtwist.cannontracer.serverside.CannonTesterCommandHandler;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandBlockExecutor.class)
public class CommandBlockExecutorMixin {

	@Inject(at = @At("HEAD"), method = "execute", cancellable = true)
	public void onExecute(World world, CallbackInfoReturnable<Boolean> info) {
		@SuppressWarnings("ConstantConditions")
		CommandBlockExecutor executor = (CommandBlockExecutor) (Object) this;

		if (CannonTesterCommandHandler.isCannonTesterCommand(executor.getCommand())) {
			CannonTesterCommandHandler.getInstance().execute(executor, executor.getCommand());
			info.setReturnValue(true);
			info.cancel();
		}
	}

}
