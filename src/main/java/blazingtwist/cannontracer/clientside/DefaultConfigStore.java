package blazingtwist.cannontracer.clientside;

import blazingtwist.cannontracer.clientside.datatype.Color;
import blazingtwist.cannontracer.clientside.datatype.EntityTrackingSettings;
import blazingtwist.cannontracer.clientside.datatype.HotKeyConfig;
import blazingtwist.cannontracer.clientside.datatype.KeyBind;
import blazingtwist.cannontracer.clientside.datatype.SessionSettings;
import blazingtwist.cannontracer.clientside.datatype.TracerConfig;
import org.lwjgl.glfw.GLFW;

public class DefaultConfigStore {

	public static SessionSettings buildDefaultSessionSettings() {
		return new SessionSettings();
	}

	public static TracerConfig buildDefaultTracerConfig() {
		TracerConfig tracerConfig = new TracerConfig();
		tracerConfig.getTrackedEntities().put(
				"tnt",
				new EntityTrackingSettings(true, 10, 1.5f, new Color(255, 0, 0, 255), 0.49)
		);
		tracerConfig.getTrackedEntities().put(
				"falling_block",
				new EntityTrackingSettings(true, 10, 1.5f, new Color(0, 255, 0, 255), 0.5)
		);

		return tracerConfig;
	}

	public static HotKeyConfig buildDefaultHotKeyConfig() {
		HotKeyConfig config = new HotKeyConfig();
		config.getHotKeys().add(new HotKeyConfig.HotKey(
				"/cts register",
				new KeyBind().setTrigger(GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_LEFT_CONTROL)
		));
		config.getHotKeys().add(new HotKeyConfig.HotKey(
				"/cts tester edit",
				new KeyBind().setTrigger(GLFW.GLFW_KEY_C).setExclude(GLFW.GLFW_KEY_LEFT_SHIFT)
		));
		config.getHotKeys().add(new HotKeyConfig.HotKey(
				"/cts activator select",
				new KeyBind().setTrigger(GLFW.GLFW_KEY_T, GLFW.GLFW_KEY_LEFT_SHIFT)
		));
		config.getHotKeys().add(new HotKeyConfig.HotKey(
				"/cts activator trigger",
				new KeyBind().setTrigger(GLFW.GLFW_KEY_T).setExclude(GLFW.GLFW_KEY_LEFT_SHIFT)
		));
		return config;
	}

}
