package blazingtwist.cannontracer.shared.datatypes;

public enum TntAlignment {
	MINUS_ONE(-0.01),
	NONE(0),
	PLUS_ONE(0.01);

	private final double blockOffset;

	TntAlignment(double blockOffset) {
		this.blockOffset = blockOffset;
	}

	public double getBlockOffset() {
		return blockOffset;
	}
}
