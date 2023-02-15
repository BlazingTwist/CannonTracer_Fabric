package blazingtwist.cannontracer.clientside;

import blazingtwist.cannontracer.clientside.datatype.EntityTrackingSettings;
import blazingtwist.cannontracer.clientside.datatype.KeyBind;
import blazingtwist.cannontracer.clientside.datatype.SessionSettings;
import blazingtwist.cannontracer.clientside.datatype.TracerConfig;
import blazingtwist.cannontracer.clientside.datatype.TracerKeyBinds;
import blazingtwist.cannontracer.clientside.gui.CannonTracerScreen;
import blazingtwist.cannontracer.clientside.gui.TracerConfigGui;
import blazingtwist.cannontracer.networking.ClientPacketHandler;
import blazingtwist.cannontracer.shared.FileManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class SettingsManager {

	private static final String tracerConfigSubPath = "cannontracer-config.json";

	private static SettingsManager instance;

	public static SettingsManager getInstance() {
		if (instance == null) {
			instance = new SettingsManager();
		}
		return instance;
	}

	private final SessionSettings sessionSettings;
	private final TracerConfig tracerConfig;
	private final List<KeyBindController> keyBindControllers;

	private boolean isRegisteredWithServer = false;

	private SettingsManager() {
		sessionSettings = DefaultConfigStore.buildDefaultSessionSettings();
		tracerConfig = FileManager.tryDeserializeToObject(
				tracerConfigSubPath,
				new TypeReference<>() {},
				DefaultConfigStore::buildDefaultTracerConfig
		);

		TracerKeyBinds binds = tracerConfig.getKeyBinds();
		keyBindControllers = new ArrayList<>();
		keyBindControllers.add(new KeyBindController(binds.openMenu, this::onKeyBind_openMenu));
		keyBindControllers.add(new KeyBindController(binds.toggleXRay, this::onKeyBind_toggleXRay));
		keyBindControllers.add(new KeyBindController(binds.togglePositionText, this::onKeyBind_togglePositionText));
		keyBindControllers.add(new KeyBindController(binds.toggleVelocityText, this::onKeyBind_toggleVelocityText));
		keyBindControllers.add(new KeyBindController(binds.pullData, this::onKeyBind_pullData));
		keyBindControllers.add(new KeyBindController(binds.clearData, this::onKeyBind_clearData));
		keyBindControllers.add(new KeyBindControllerWithRepetition(binds.displayTickIncrement, this::onKeyBind_displayTickIncrement));
		keyBindControllers.add(new KeyBindControllerWithRepetition(binds.displayTickDecrement, this::onKeyBind_displayTickDecrement));
		keyBindControllers.add(new KeyBindController(binds.displayNextDespawnTick, this::onKeyBind_displayNextDespawnTick));
		keyBindControllers.add(new KeyBindController(binds.displayPrevDespawnTick, this::onKeyBind_displayPrevDespawnTick));
		keyBindControllers.add(new KeyBindController(binds.displayFirstTick, this::onKeyBind_displayFirstTick));
		keyBindControllers.add(new KeyBindController(binds.displayLastTick, this::onKeyBind_displayLastTick));

		InputManager.getInstance().addPressKeyListener(this::updateKeyBinds);
		InputManager.getInstance().addReleaseKeyListener(this::updateKeyBinds);
	}

	public TracerConfig getTracerConfig() {
		return tracerConfig;
	}

	public SessionSettings getSessionSettings() {
		return sessionSettings;
	}

	public void onQuitFromServer() {
		isRegisteredWithServer = false;
		sessionSettings.setRenderTick(0);
		TraceRenderer.getInstance().clearTraces();
		saveSettingsToFile();
	}

	public void onServerRegistered() {
		isRegisteredWithServer = true;
	}

	public boolean isRegisteredWithServer() {
		return isRegisteredWithServer;
	}

	public EntityTrackingSettings getEntitySettings(String entityTypeName) {
		return tracerConfig.getTrackedEntities().get(entityTypeName);
	}

	public boolean xRayTraces() {
		return tracerConfig.isXRayTraces();
	}

	public long getRenderTick() {
		return sessionSettings.getRenderTick();
	}

	public void saveSettingsToFile() {
		String tracerConfigJson = FileManager.objectToJson(tracerConfig);
		if (tracerConfigJson == null) {
			onSaveSettingsFailed();
			return;
		}

		if (!FileManager.saveToFile(tracerConfigSubPath, tracerConfigJson)) {
			onSaveSettingsFailed();
		}
	}

	private void onSaveSettingsFailed() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player != null) {
			player.sendMessage(Text.literal("Failed to save config. Open and close the gui again to retry."));
		}
	}

	private void updateKeyBinds(int keyChanged) {
		keyBindControllers.forEach(controller -> controller.updateBind(keyChanged));
		tracerConfig.getHotKeys().updateHotKeys(keyChanged);
	}

	private void onKeyBind_openMenu() {
		RenderSystem.recordRenderCall(() -> MinecraftClient.getInstance().setScreen(new CannonTracerScreen(TracerConfigGui.getInstance())));
	}

	private void onKeyBind_toggleXRay() {
		tracerConfig.setXRayTraces(!tracerConfig.isXRayTraces());
	}

	private void onKeyBind_togglePositionText() {
		tracerConfig.setDrawPositionText(!tracerConfig.isDrawPositionText());
	}

	private void onKeyBind_toggleVelocityText() {
		tracerConfig.setDrawVelocityText(!tracerConfig.isDrawVelocityText());
	}

	private void onKeyBind_pullData() {
		ClientPacketHandler.getInstance().clientToServer_requestTraceData();
	}

	private void onKeyBind_clearData() {
		TraceRenderer.getInstance().clearTraces();
	}

	private void onKeyBind_displayTickIncrement() {
		sessionSettings.setRenderTick(sessionSettings.getRenderTick() + 1);
	}

	private void onKeyBind_displayTickDecrement() {
		sessionSettings.setRenderTick(sessionSettings.getRenderTick() - 1);
	}

	private void onKeyBind_displayNextDespawnTick() {
		Set<Long> despawnTicks = TraceRenderer.getInstance().findDespawnTicks();
		if (despawnTicks.isEmpty()) {
			return;
		}

		long currentTick = sessionSettings.getRenderTick();
		long minGreaterTick = Long.MAX_VALUE;
		long minDespawnTick = Long.MAX_VALUE;
		for (Long tick : despawnTicks) {
			if (tick > currentTick) {
				minGreaterTick = Math.min(minGreaterTick, tick);
			}
			minDespawnTick = Math.min(minDespawnTick, tick);
		}

		// wrap around to first despawnTick if no greater tick found
		long targetTick = minGreaterTick != Long.MAX_VALUE ? minGreaterTick : minDespawnTick;
		sessionSettings.setRenderTick(targetTick);
	}

	private void onKeyBind_displayPrevDespawnTick() {
		Set<Long> despawnTicks = TraceRenderer.getInstance().findDespawnTicks();
		if (despawnTicks.isEmpty()) {
			return;
		}

		long currentTick = sessionSettings.getRenderTick();
		long maxLesserTick = Long.MIN_VALUE;
		long maxDespawnTick = Long.MIN_VALUE;
		for (Long tick : despawnTicks) {
			if (tick < currentTick) {
				maxLesserTick = Math.max(maxLesserTick, tick);
			}
			maxDespawnTick = Math.max(maxDespawnTick, tick);
		}

		// wrap around to first despawnTick if no greater tick found
		long targetTick = maxLesserTick != Long.MIN_VALUE ? maxLesserTick : maxDespawnTick;
		sessionSettings.setRenderTick(targetTick);
	}

	private void onKeyBind_displayFirstTick() {
		TraceRenderer.getInstance().findMinTick().ifPresent(sessionSettings::setRenderTick);
	}

	private void onKeyBind_displayLastTick() {
		TraceRenderer.getInstance().findMaxTick().ifPresent(sessionSettings::setRenderTick);
	}

	private static class KeyBindController {
		protected final KeyBind bind;
		protected final Runnable onPressed;

		protected boolean currentlySatisfied;

		public KeyBindController(KeyBind bind, Runnable onPressed) {
			this.bind = bind;
			this.onPressed = onPressed;
		}

		public void updateBind(int keyChanged) {
			if (bind.isKeyRelevant(keyChanged)) {
				setSatisfied(bind.computeIsSatisfied());
			}
		}

		protected void setSatisfied(boolean satisfied) {
			if (!currentlySatisfied && satisfied) {
				onPressed.run();
			}
			currentlySatisfied = satisfied;
		}
	}

	private static class KeyBindControllerWithRepetition extends KeyBindController {
		private static final int repeatDelayMs = 400;
		private static final int repeatRateMs = 40;

		private Timer repeatTimer;

		public KeyBindControllerWithRepetition(KeyBind bind, Runnable onPressed) {
			super(bind, onPressed);
		}

		@Override
		protected void setSatisfied(boolean satisfied) {
			super.setSatisfied(satisfied);
			queueRepetition(repeatDelayMs);
		}

		private void onRepeat() {
			if (!currentlySatisfied) {
				return;
			}
			onPressed.run();
			queueRepetition(repeatRateMs);
		}

		private void queueRepetition(int delayMillis) {
			if (repeatTimer != null) {
				repeatTimer.cancel();
				repeatTimer.purge();
			}
			repeatTimer = new Timer();
			repeatTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					onRepeat();
				}
			}, delayMillis);
		}
	}
}
