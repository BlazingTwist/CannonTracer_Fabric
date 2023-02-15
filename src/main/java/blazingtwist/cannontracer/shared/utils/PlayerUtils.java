package blazingtwist.cannontracer.shared.utils;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class PlayerUtils {

	public static BlockPos getLookedAtBlockPos(ServerPlayerEntity player, double range) {
		HitResult hitResult = player.raycast(range, 1f, false);
		if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
			return null;
		}

		if (hitResult instanceof BlockHitResult blockHitResult) {
			return blockHitResult.getBlockPos();
		}

		return null;
	}

	public static BlockState getBlockState(ServerPlayerEntity player, BlockPos pos) {
		if (pos == null) {
			return null;
		}
		return player.world.getBlockState(pos);
	}

}
