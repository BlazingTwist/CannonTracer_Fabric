package blazingtwist.cannontracer.shared.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

public class ChatUtils {

	public static void sendMessage(String message, boolean addToHistory) {
		message = StringHelper.truncateChat(StringUtils.normalizeSpace(message.trim()));

		MinecraftClient client = MinecraftClient.getInstance();
		if (addToHistory) {
			client.inGameHud.getChatHud().addToMessageHistory(message);
		}

		assert client.player != null;
		if (message.startsWith("/")) {
			client.player.networkHandler.sendChatCommand(message.substring(1));
		} else {
			client.player.networkHandler.sendChatMessage(message);
		}
	}

}
