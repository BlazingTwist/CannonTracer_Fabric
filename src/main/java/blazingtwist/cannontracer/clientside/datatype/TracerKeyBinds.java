package blazingtwist.cannontracer.clientside.datatype;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.lwjgl.glfw.GLFW;

public class TracerKeyBinds {

	@JsonProperty("openMenu")
	public final KeyBind openMenu = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("toggleXRay")
	public final KeyBind toggleXRay = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("togglePositionText")
	public final KeyBind togglePositionText = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_P, GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("toggleVelocityText")
	public final KeyBind toggleVelocityText = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("pullData")
	public final KeyBind pullData = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_R)
			.addExclude(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL);

	@JsonProperty("clearData")
	public final KeyBind clearData = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_LEFT_SHIFT)
			.addExclude(GLFW.GLFW_KEY_LEFT_CONTROL);

	@JsonProperty("displayTickIncrement")
	public final KeyBind displayTickIncrement = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_RIGHT)
			.addExclude(GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("displayTickDecrement")
	public final KeyBind displayTickDecrement = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_LEFT)
			.addExclude(GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("displayNextDespawnTick")
	public final KeyBind displayNextDespawnTick = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("displayPrevDespawnTick")
	public final KeyBind displayPrevDespawnTick = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_LEFT_SHIFT);

	@JsonProperty("displayFirstTick")
	public final KeyBind displayFirstTick = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_UP);

	@JsonProperty("displayLastTick")
	public final KeyBind displayLastTick = new KeyBind()
			.addTrigger(GLFW.GLFW_KEY_DOWN);

}
