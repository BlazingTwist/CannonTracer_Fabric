package blazingtwist.cannontracer.serverside;

import blazingtwist.cannontracer.networking.ServerPacketHandler;
import blazingtwist.cannontracer.serverside.datatype.EntityDataChain;
import blazingtwist.cannontracer.serverside.datatype.PlayerSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class EntityTracker {

	private static EntityTracker instance;

	public static EntityTracker getInstance() {
		if (instance == null) {
			instance = new EntityTracker();
		}
		return instance;
	}

	public static String getEntityTypeName(Entity entity) {
		return entity.getType().getUntranslatedName();
	}

	private final AtomicLong currentServerTick = new AtomicLong(0);
	private final AtomicReference<Map<String, Float>> memoryTime = new AtomicReference<>(Collections.emptyMap());

	private final Object trackingDataLock = new Object();
	private final ArrayList<Entity> trackedEntities = new ArrayList<>();
	/** maps entityTypeName -> entityID -> entityTrace */
	private final HashMap<String, HashMap<Integer, EntityDataChain>> tracingHistory = new HashMap<>();

	private EntityTracker() {
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
		TracerPlayerManager.getInstance().addChangeListener(this::onPlayerSettingsChanged);
	}

	private void onServerTick(MinecraftServer server) {
		long serverTick = currentServerTick.getAndIncrement();
		Map<String, Float> memoryTimeMap = memoryTime.get();

		synchronized (trackingDataLock) {
			for (Map.Entry<String, HashMap<Integer, EntityDataChain>> entityTraceEntry : tracingHistory.entrySet()) {
				String entityTypeName = entityTraceEntry.getKey();
				HashMap<Integer, EntityDataChain> entityTrace = entityTraceEntry.getValue();

				float memorySeconds = memoryTimeMap.getOrDefault(entityTypeName, 5f);
				long memoryGameTicks = (long) (memorySeconds * 20);

				entityTrace.entrySet().removeIf(traceEntry -> (serverTick - traceEntry.getValue().creationTick) >= memoryGameTicks);
			}

			if (trackedEntities.isEmpty()) {
				return;
			}

			for (Iterator<Entity> entityIterator = trackedEntities.iterator(); entityIterator.hasNext(); ) {
				Entity entity = entityIterator.next();
				if (entity.isAlive()) {
					continue;
				}

				entityIterator.remove();
				HashMap<Integer, EntityDataChain> entityTrace = tracingHistory.get(getEntityTypeName(entity));
				if (entityTrace != null) {
					entityTrace.computeIfAbsent(entity.getId(), id -> new EntityDataChain(serverTick))
							.addLinkAtEnd(entity.getPos(), entity.getVelocity());
				}
			}
		}
	}

	public void onEntityMove(Entity entity, Vec3d velocity) {
		Map<String, Float> memoryTimeMap = memoryTime.get();
		String entityTypeName = getEntityTypeName(entity);
		if (!TracerPlayerManager.getInstance().isListeningForEntity(entityTypeName) || memoryTimeMap.get(entityTypeName) <= 0) {
			return;
		}

		long serverTick = currentServerTick.get();
		synchronized (trackingDataLock) {
			HashMap<Integer, EntityDataChain> entityTrace = tracingHistory.get(entityTypeName);
			entityTrace.computeIfAbsent(entity.getId(), id -> new EntityDataChain(serverTick))
					.addLinkAtEnd(entity.getPos(), velocity);
		}
	}

	public void onEntitySpawn(Entity entity) {
		TracerPlayerManager.getInstance().notifyPlayers_entitySpawn(entity);

		Map<String, Float> memoryTimeMap = memoryTime.get();
		String entityTypeName = getEntityTypeName(entity);
		if (TracerPlayerManager.getInstance().isListeningForEntity(entityTypeName) && memoryTimeMap.get(entityTypeName) > 0) {
			synchronized (trackingDataLock) {
				trackedEntities.add(entity);
			}
		}
	}

	private void onPlayerSettingsChanged() {
		synchronized (trackingDataLock) {
			tracingHistory.keySet().removeIf(key -> !TracerPlayerManager.getInstance().isListeningForEntity(key));
			for (String entityName : TracerPlayerManager.getInstance().getObservedEntities()) {
				tracingHistory.computeIfAbsent(entityName, (name) -> new HashMap<>());
			}
		}

		memoryTime.set(TracerPlayerManager.getInstance().generateMaxEntityTimes());
	}

	public void onPlayerRequestTraceData(ServerPlayerEntity player) {
		PlayerSettings playerSettings = TracerPlayerManager.getInstance().getPlayerSettings(player);
		if (playerSettings == null) {
			player.sendMessage(Text.literal("PullData failed! It looks like you're not registered, try re-logging.").formatted(Formatting.RED));
			return;
		}

		long requestServerTick = currentServerTick.get();
		Vec3d playerPosition = player.getPos();

		new Thread(() -> {
			Map<String, List<EntityDataChain>> relevantEntitiesByTypeName;
			synchronized (trackingDataLock) {
				relevantEntitiesByTypeName = playerSettings.observedEntities.entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, entry -> {
							long memoryTicks = (long) (entry.getValue() * 20);
							return tracingHistory.get(entry.getKey()).values().stream()
									.filter(chain -> chain.creationTick + memoryTicks >= requestServerTick
											&& chain.isInRange(playerPosition, playerSettings.maxRange))
									.collect(Collectors.toList());
						}));
			}

			long firstEntityTick = relevantEntitiesByTypeName.values().stream()
					.flatMapToLong(chains -> chains.stream().mapToLong(chain -> chain.creationTick))
					.min().orElse(0);

			ServerPacketHandler.getInstance().serverToClient_sendTraceData(player, firstEntityTick, relevantEntitiesByTypeName);
		}).start();
	}
}
