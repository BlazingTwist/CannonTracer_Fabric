package blazingtwist.cannontracer.serverside.datatype;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.Box;

public class RegionConfig {

	@JsonProperty("globalTnt")
	private final boolean globalTnt;

	@JsonProperty("regionsByName")
	private final HashMap<String, RegionSave> regionsByName = new HashMap<>();

	public RegionConfig() {
		globalTnt = true;
	}

	public RegionConfig(boolean globalTnt, HashMap<String, Region> regionsByName) {
		this.globalTnt = globalTnt;
		for (Map.Entry<String, Region> entry : regionsByName.entrySet()) {
			this.regionsByName.put(entry.getKey(), RegionSave.fromRegion(entry.getValue()));
		}
	}

	public boolean isGlobalTnt() {
		return globalTnt;
	}

	public HashMap<String, Region> toRegionData() {
		HashMap<String, Region> result = new HashMap<>();
		for (Map.Entry<String, RegionSave> entry : this.regionsByName.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toRegion());
		}
		return result;
	}

	private static record RegionSave(
			@JsonProperty("box") BoxSave boundingBox,
			@JsonProperty("tnt") boolean tntDisabled) {

		public static RegionSave fromRegion(Region region) {
			return new RegionSave(BoxSave.fromBox(region.getBoundingBox()), region.isTntDisabled());
		}

		public Region toRegion() {
			Region region = new Region(boundingBox.toBox());
			region.setTntDisabled(tntDisabled);
			return region;
		}
	}

	private static record BoxSave(
			@JsonProperty("x0") double x0,
			@JsonProperty("y0") double y0,
			@JsonProperty("z0") double z0,
			@JsonProperty("x1") double x1,
			@JsonProperty("y1") double y1,
			@JsonProperty("z1") double z1) {

		public static BoxSave fromBox(Box box) {
			return new BoxSave(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		}

		public Box toBox() {
			return new Box(x0, y0, z0, x1, y1, z1);
		}
	}

}
