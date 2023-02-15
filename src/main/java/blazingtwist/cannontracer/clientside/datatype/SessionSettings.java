package blazingtwist.cannontracer.clientside.datatype;

/**
 * Contains settings that only apply to this session and are not saved.
 */
public class SessionSettings {

	private long renderTick = 0;

	public long getRenderTick() {
		return renderTick;
	}

	public SessionSettings setRenderTick(long renderTick) {
		this.renderTick = renderTick;
		return this;
	}

}
