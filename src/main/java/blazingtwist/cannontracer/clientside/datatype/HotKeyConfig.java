package blazingtwist.cannontracer.clientside.datatype;

import blazingtwist.cannontracer.shared.utils.ChatUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class HotKeyConfig {

	@JsonProperty("hotKeys")
	private final List<HotKey> hotKeys = new ArrayList<>();

	public HotKeyConfig() {
	}

	public List<HotKey> getHotKeys() {
		return hotKeys;
	}

	public void updateHotKeys(int keyChanged) {
		for (HotKeyConfig.HotKey hotKey : hotKeys) {
			KeyBind bind = hotKey.getBind();
			if (bind.isKeyRelevant(keyChanged) && bind.computeIsSatisfied()) {
				ChatUtils.sendMessage(hotKey.getCommand(), false);
			}
		}
	}

	public static class HotKey {
		@JsonProperty("command")
		private String command;

		@JsonProperty("bind")
		private final KeyBind bind;

		private HotKey() {
			bind = null;
		}

		public HotKey(String command, KeyBind bind) {
			this.command = command;
			this.bind = bind;
		}

		public String getCommand() {
			return command;
		}

		public HotKey setCommand(String command) {
			this.command = command;
			return this;
		}

		public KeyBind getBind() {
			return bind;
		}
	}

}
