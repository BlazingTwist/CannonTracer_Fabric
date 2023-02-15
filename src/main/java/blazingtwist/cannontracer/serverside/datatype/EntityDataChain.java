package blazingtwist.cannontracer.serverside.datatype;

import java.util.ArrayList;
import net.minecraft.util.math.Vec3d;

public class EntityDataChain {

	public final long creationTick;
	public final ArrayList<ChainLink> links;

	public EntityDataChain(long serverTick) {
		this.creationTick = serverTick;
		this.links = new ArrayList<>();
	}

	public EntityDataChain(long serverTick, Vec3d position, Vec3d velocity) {
		this.creationTick = serverTick;
		this.links = new ArrayList<>();
		links.add(new ChainLink(position, velocity));
	}

	public void addLinkAtEnd(Vec3d position, Vec3d velocity) {
		ChainLink link = new ChainLink(position, velocity);
		links.add(link);
	}

	public boolean isInRange(Vec3d position, int blockDistance) {
		if (blockDistance < 0) {
			return true;
		}

		double squaredMaxDistance = blockDistance * blockDistance;
		return links.stream().anyMatch(link -> link.position.squaredDistanceTo(position) < squaredMaxDistance);
	}

	public record ChainLink(Vec3d position, Vec3d velocity) {
	}

}
