package blazingtwist.cannontracer.serverside.datatype;

import java.util.HashMap;

public class PlayerSettings {
	public final HashMap<String, Float> observedEntities = new HashMap<>();
	public boolean listenToEntitySpawns = false;
	public int maxRange = -1;
}
