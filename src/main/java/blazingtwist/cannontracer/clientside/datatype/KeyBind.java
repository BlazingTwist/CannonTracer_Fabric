package blazingtwist.cannontracer.clientside.datatype;

import blazingtwist.cannontracer.clientside.InputManager;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class KeyBind {
	@JsonProperty("trigger")
	private final List<Integer> trigger = new ArrayList<>();

	@JsonProperty("exclude")
	private final List<Integer> exclude = new ArrayList<>();

	public KeyBind() {
	}

	public List<Integer> getTrigger() {
		return trigger;
	}

	public List<Integer> getExclude() {
		return exclude;
	}

	public KeyBind setTrigger(int... triggers) {
		this.trigger.clear();
		return addTrigger(triggers);
	}

	public KeyBind addTrigger(int... triggers) {
		for (int trig : triggers) {
			this.trigger.add(trig);
		}
		return this;
	}

	public void removeTrigger(int trigger) {
		this.trigger.remove(Integer.valueOf(trigger));
	}

	public KeyBind setExclude(int... excludes) {
		this.exclude.clear();
		return addExclude(excludes);
	}

	public KeyBind addExclude(int... excludes) {
		for (int excl : excludes) {
			this.exclude.add(excl);
		}
		return this;
	}

	public void removeExclude(int exclude) {
		this.exclude.remove(Integer.valueOf(exclude));
	}

	public boolean isKeyRelevant(int key) {
		return trigger.contains(key) || exclude.contains(key);
	}

	public boolean computeIsSatisfied() {
		if (trigger.isEmpty()) {
			// prevent keyBind from auto-firing.
			return false;
		}

		InputManager inputManager = InputManager.getInstance();
		return inputManager.areKeysPressed(trigger)
				&& inputManager.areKeysReleased(exclude);
	}
}
