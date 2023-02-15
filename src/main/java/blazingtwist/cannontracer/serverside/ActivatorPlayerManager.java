package blazingtwist.cannontracer.serverside;

import java.util.HashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ActivatorPlayerManager {

	private static ActivatorPlayerManager instance;

	public static ActivatorPlayerManager getInstance() {
		if (instance == null) {
			instance = new ActivatorPlayerManager();
		}
		return instance;
	}

	private final HashMap<ServerPlayerEntity, BlockPos> playerBlockSelections = new HashMap<>();

	private ActivatorPlayerManager() {
	}

	public void playerSelectsBlock(ServerPlayerEntity player, BlockPos pos) {
		playerBlockSelections.put(player, pos);
	}

	public BlockPos getSelectedBlock(ServerPlayerEntity player) {
		return playerBlockSelections.get(player);
	}

}
