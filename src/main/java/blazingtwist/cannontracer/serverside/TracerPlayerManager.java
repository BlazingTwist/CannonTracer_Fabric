package blazingtwist.cannontracer.serverside;

import blazingtwist.cannontracer.networking.ServerPacketHandler;
import blazingtwist.cannontracer.serverside.datatype.PlayerSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TracerPlayerManager {

	private static TracerPlayerManager instance;

	public static TracerPlayerManager getInstance() {
		if (instance == null) {
			instance = new TracerPlayerManager();
		}
		return instance;
	}

	private final HashMap<ServerPlayerEntity, PlayerSettings> playerSettings = new HashMap<>();
	private final ArrayList<Runnable> settingsChangeListeners = new ArrayList<>();
	private final AtomicReference<List<ServerPlayerEntity>> playersListeningForSpawns = new AtomicReference<>(Collections.emptyList());

	private TracerPlayerManager() {
	}

	public boolean isPlayerRegistered(ServerPlayerEntity player) {
		synchronized (playerSettings) {
			return playerSettings.containsKey(player);
		}
	}

	public PlayerSettings getPlayerSettings(ServerPlayerEntity player) {
		synchronized (playerSettings) {
			return playerSettings.get(player);
		}
	}

	public void updatePlayerSettings(ServerPlayerEntity player, PlayerSettings settings) {
		synchronized (playerSettings) {
			playerSettings.put(player, settings);
			onSettingsChanged();
		}
	}

	public void registerPlayer(ServerPlayerEntity player) {
		ServerPacketHandler.getInstance().serverToClient_requestClientConfig(player);
	}

	public void onPlayerQuit(ServerPlayerEntity player) {
		unregisterPlayer(player);
	}

	public void unregisterPlayer(ServerPlayerEntity player) {
		boolean didRemovePlayer;
		synchronized (playerSettings) {
			didRemovePlayer = playerSettings.remove(player) != null;
		}
		if (didRemovePlayer) {
			onSettingsChanged();
		}
	}

	public Set<ServerPlayerEntity> getRegisteredPlayers() {
		synchronized (playerSettings) {
			return new HashSet<>(playerSettings.keySet());
		}
	}

	public void addChangeListener(Runnable callback) {
		settingsChangeListeners.add(callback);
	}

	private void onSettingsChanged() {
		updateListeningPlayersList();
		notifyChangeListeners();
	}

	private void updateListeningPlayersList() {
		List<ServerPlayerEntity> listeningPlayers;
		synchronized (playerSettings) {
			listeningPlayers = playerSettings.entrySet().stream()
					.filter(playerEntry -> playerEntry.getValue().listenToEntitySpawns)
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());
		}

		playersListeningForSpawns.set(listeningPlayers);
	}

	private void notifyChangeListeners() {
		for (Runnable listener : settingsChangeListeners) {
			listener.run();
		}
	}

	public void notifyPlayers_entitySpawn(Entity entity) {
		List<ServerPlayerEntity> listeningPlayers = playersListeningForSpawns.get();
		if (listeningPlayers.isEmpty()) {
			return;
		}

		String entityName = EntityTracker.getEntityTypeName(entity);
		String positionString = entity.getPos().toString();
		MutableText message = Text.literal("Found entity ").formatted(Formatting.GREEN)
				.append(Text.literal(entityName).formatted(Formatting.AQUA))
				.append(Text.literal(" at position ").formatted(Formatting.GREEN))
				.append(Text.literal(positionString).formatted(Formatting.AQUA));

		for (ServerPlayerEntity listeningPlayer : listeningPlayers) {
			listeningPlayer.sendMessage(message);
		}
	}

	public List<String> getObservedEntities() {
		synchronized (playerSettings) {
			return playerSettings.values().stream()
					.flatMap(settings -> settings.observedEntities.keySet().stream())
					.collect(Collectors.toList());
		}
	}

	public boolean isListeningForEntity(String entityTypeName) {
		synchronized (playerSettings) {
			for (PlayerSettings settings : playerSettings.values()) {
				if (settings.observedEntities.containsKey(entityTypeName)) {
					return true;
				}
			}
		}
		return false;
	}

	public Map<String, Float> generateMaxEntityTimes() {
		synchronized (playerSettings) {
			return playerSettings.values().stream()
					.flatMap(setting -> setting.observedEntities.entrySet().stream())
					.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.collectingAndThen(
							Collectors.toList(),
							entryList -> entryList.stream().map(Map.Entry::getValue).max(Float::compare).orElse(0f)
					)));
		}
	}
}
