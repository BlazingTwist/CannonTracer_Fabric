package blazingtwist.cannontracer.serverside;

import blazingtwist.cannontracer.serverside.datatype.PlayerSelection;
import blazingtwist.cannontracer.serverside.datatype.Region;
import blazingtwist.cannontracer.serverside.datatype.RegionConfig;
import blazingtwist.cannontracer.shared.FileManager;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class RegionManager {

	private static final String regionConfigPath = "cannontracer-regions.json";

	private static RegionManager instance;

	public static RegionManager getInstance() {
		if (instance == null) {
			instance = new RegionManager();
		}
		return instance;
	}

	private final HashMap<ServerPlayerEntity, PlayerSelection> playerSelections = new HashMap<>();
	private boolean global_allowTnt;
	private final HashMap<String, Region> regionsByName;

	private RegionManager() {
		RegionConfig regionConfig = FileManager.tryDeserializeToObject(regionConfigPath, new TypeReference<>() {}, RegionConfig::new);
		global_allowTnt = regionConfig.isGlobalTnt();
		regionsByName = regionConfig.toRegionData();

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveConfig());
	}

	public void saveConfig() {
		String json = FileManager.objectToJson(new RegionConfig(global_allowTnt, regionsByName));
		if (json != null) {
			FileManager.saveToFile(regionConfigPath, json);
		}
	}

	public void setPos1(ServerPlayerEntity player, BlockPos pos1) {
		playerSelections.computeIfAbsent(player, x -> new PlayerSelection()).setPos1(pos1);
	}

	public void setPos2(ServerPlayerEntity player, BlockPos pos2) {
		playerSelections.computeIfAbsent(player, x -> new PlayerSelection()).setPos2(pos2);
	}

	public PlayerSelection getPlayerSelection(ServerPlayerEntity player) {
		return playerSelections.get(player);
	}

	public Region createRegion(String name, PlayerSelection selection) {
		Region existingRegion = regionsByName.get(name);
		if (existingRegion != null) {
			existingRegion.setBoundingBox(selection.getRegionBox());
			return existingRegion;
		}

		Region region = new Region(selection.getRegionBox());
		regionsByName.put(name, region);
		return region;
	}

	public Region getRegion(String name) {
		return regionsByName.get(name);
	}

	/**
	 * @param name name of the region
	 * @return true if the region did exist before deletion, false otherwise.
	 */
	public boolean deleteRegion(String name) {
		return regionsByName.remove(name) != null;
	}

	public List<String> getRegionNames() {
		return new ArrayList<>(regionsByName.keySet());
	}

	public List<String> getRegionNames(Predicate<Region> predicate) {
		return regionsByName.entrySet().stream()
				.filter(entry -> predicate.test(entry.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	public boolean toggleGlobalTnt() {
		global_allowTnt = !global_allowTnt;
		return global_allowTnt;
	}

	public boolean shouldNullifyExplosionDamage(double x, double y, double z) {
		boolean shouldNullify = !global_allowTnt;
		long resultPriority = Long.MAX_VALUE;

		for (Region region : regionsByName.values()) {
			long priority = region.computePriority();
			if (priority >= resultPriority) {
				continue;
			}

			if (!region.getBoundingBox().contains(x, y, z)) {
				continue;
			}

			shouldNullify = region.isTntDisabled();
			resultPriority = priority;
		}

		return shouldNullify;
	}

	public boolean isProtected(BlockPos pos) {
		double x = pos.getX() + 0.5d;
		double y = pos.getY() + 0.5d;
		double z = pos.getZ() + 0.5d;
		return shouldNullifyExplosionDamage(x, y, z);
	}

}
