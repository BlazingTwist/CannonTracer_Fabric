package blazingtwist.cannontracer.clientside;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class InputManager {

	private static InputManager instance;

	public static InputManager getInstance() {
		if (instance == null) {
			instance = new InputManager();
		}
		return instance;
	}

	private final List<Consumer<Integer>> repeatKeyConsumers = new ArrayList<>();
	private final List<Consumer<Integer>> pressKeyConsumers = new ArrayList<>();
	private final List<Consumer<Integer>> releaseKeyConsumers = new ArrayList<>();
	private final HashSet<Integer> pressedKeys = new HashSet<>();

	private InputManager() {
	}

	public void addRepeatKeyListener(Consumer<Integer> listener) {
		this.repeatKeyConsumers.add(listener);
	}

	public void addPressKeyListener(Consumer<Integer> listener) {
		this.pressKeyConsumers.add(listener);
	}

	public void addReleaseKeyListener(Consumer<Integer> listener) {
		this.releaseKeyConsumers.add(listener);
	}

	public boolean areKeysPressed(int... keys) {
		if (keys == null || keys.length == 0) {
			return true;
		}

		synchronized (pressedKeys) {
			for (int key : keys) {
				if (!pressedKeys.contains(key)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean areKeysPressed(Collection<Integer> keys) {
		if (keys == null || keys.isEmpty()) {
			return true;
		}

		synchronized (pressedKeys) {
			return pressedKeys.containsAll(keys);
		}
	}

	public boolean areKeysReleased(int... keys) {
		if (keys == null || keys.length == 0) {
			return true;
		}

		synchronized (pressedKeys) {
			for (int key : keys) {
				if (pressedKeys.contains(key)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean areKeysReleased(Collection<Integer> keys) {
		if (keys == null || keys.isEmpty()) {
			return true;
		}

		synchronized (pressedKeys) {
			for (int key : keys) {
				if (pressedKeys.contains(key)) {
					return false;
				}
			}
		}
		return true;
	}

	public void onKeyPressed(int key, int scancode, int action, int modifiers) {
		if (MinecraftClient.getInstance().world == null
				|| MinecraftClient.getInstance().currentScreen != null) {
			//clear pressed keys to prevent triggering key-binds in the main menu / in GUIs.
			synchronized (pressedKeys) {
				pressedKeys.clear();
			}
			return;
		}

		if (action == GLFW.GLFW_REPEAT) {
			notifyRepeatKey(key);
		} else if (action == GLFW.GLFW_PRESS) {
			synchronized (pressedKeys) {
				pressedKeys.add(key);
			}
			notifyPressKey(key);
		} else if (action == GLFW.GLFW_RELEASE) {
			synchronized (pressedKeys) {
				pressedKeys.remove(key);
			}
			notifyReleaseKey(key);
		}
	}

	private void notifyRepeatKey(int key) {
		for (Consumer<Integer> listener : repeatKeyConsumers) {
			listener.accept(key);
		}
	}

	private void notifyPressKey(int key) {
		for (Consumer<Integer> listener : pressKeyConsumers) {
			listener.accept(key);
		}
	}

	private void notifyReleaseKey(int key) {
		for (Consumer<Integer> listener : releaseKeyConsumers) {
			listener.accept(key);
		}
	}

}
