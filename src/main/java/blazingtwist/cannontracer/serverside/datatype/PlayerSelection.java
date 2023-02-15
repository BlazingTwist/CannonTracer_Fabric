package blazingtwist.cannontracer.serverside.datatype;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class PlayerSelection {
	private BlockPos pos1 = null;
	private BlockPos pos2 = null;
	private Box regionBox = null;

	public BlockPos getPos1() {
		return pos1;
	}

	public BlockPos getPos2() {
		return pos2;
	}

	public Box getRegionBox() {
		return regionBox;
	}

	public void setPos1(BlockPos pos1) {
		this.pos1 = pos1;
		recomputeBoundingBox();
	}

	public void setPos2(BlockPos pos2) {
		this.pos2 = pos2;
		recomputeBoundingBox();
	}

	private void recomputeBoundingBox() {
		if (pos1 == null || pos2 == null) {
			return;
		}
		regionBox = new Box(pos1.toCenterPos(), pos2.toCenterPos()).expand(0.5d);
	}
}
