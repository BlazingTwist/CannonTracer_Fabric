package blazingtwist.cannontracer.shared.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class TestCannonData {

	@JsonProperty("charges")
	private final List<CannonCharge> charges = new ArrayList<>();

	@JsonProperty("blockOffset")
	private FinalVec3d blockOffset;

	@JsonProperty("pixelOffset")
	private FinalVec3d pixelOffset;

	@JsonProperty("velocity")
	private FinalVec3d velocity;

	@JsonProperty("xAlign")
	private TntAlignment xAlign;

	@JsonProperty("yAlign")
	private TntAlignment yAlign;

	@JsonProperty("zAlign")
	private TntAlignment zAlign;

	public TestCannonData() {
		blockOffset = new FinalVec3d(0, 0, 0);
		pixelOffset = new FinalVec3d(0, 0, 0);
		velocity = new FinalVec3d(0, 0, 0);
		xAlign = TntAlignment.NONE;
		yAlign = TntAlignment.NONE;
		zAlign = TntAlignment.NONE;
	}

	public TestCannonData(FinalVec3d blockOffset, FinalVec3d pixelOffset, FinalVec3d velocity, TntAlignment xAlign, TntAlignment yAlign, TntAlignment zAlign) {
		this.blockOffset = blockOffset;
		this.pixelOffset = pixelOffset;
		this.velocity = velocity;
		this.xAlign = xAlign;
		this.yAlign = yAlign;
		this.zAlign = zAlign;
	}

	public List<CannonCharge> getCharges() {
		return charges;
	}

	public FinalVec3d getBlockOffset() {
		return blockOffset;
	}

	public TestCannonData setBlockOffset(FinalVec3d blockOffset) {
		this.blockOffset = blockOffset;
		return this;
	}

	public FinalVec3d getPixelOffset() {
		return pixelOffset;
	}

	public TestCannonData setPixelOffset(FinalVec3d pixelOffset) {
		this.pixelOffset = pixelOffset;
		return this;
	}

	public FinalVec3d getVelocity() {
		return velocity;
	}

	public TestCannonData setVelocity(FinalVec3d velocity) {
		this.velocity = velocity;
		return this;
	}

	public TntAlignment getXAlign() {
		return xAlign;
	}

	public TestCannonData setXAlign(TntAlignment xAlign) {
		this.xAlign = xAlign;
		return this;
	}

	public TntAlignment getYAlign() {
		return yAlign;
	}

	public TestCannonData setYAlign(TntAlignment yAlign) {
		this.yAlign = yAlign;
		return this;
	}

	public TntAlignment getZAlign() {
		return zAlign;
	}

	public TestCannonData setZAlign(TntAlignment zAlign) {
		this.zAlign = zAlign;
		return this;
	}

	public static class CannonCharge {
		@JsonProperty("enabled")
		private boolean enabled;

		@JsonProperty("delay")
		private int delay;

		@JsonProperty("amount")
		private int amount;

		@JsonProperty("note")
		private String note;

		public CannonCharge() {
		}

		public CannonCharge(boolean enabled, int delay, int amount, String note) {
			this.enabled = enabled;
			this.delay = delay;
			this.amount = amount;
			this.note = note;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public CannonCharge setEnabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public int getDelay() {
			return delay;
		}

		public CannonCharge setDelay(int delay) {
			this.delay = delay;
			return this;
		}

		public int getAmount() {
			return amount;
		}

		public CannonCharge setAmount(int amount) {
			this.amount = amount;
			return this;
		}

		public String getNote() {
			return note;
		}

		public CannonCharge setNote(String note) {
			this.note = note;
			return this;
		}
	}

}

