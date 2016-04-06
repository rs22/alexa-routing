package routing;

import routing.ActionInvocationContext;

import java.util.List;

public class IntentActivationContext {
	private String intentName;
	private ActionInvocationContext actionInvocationContext;
	private List<String> slots;
	
	public String getIntentName() { return intentName; }
	public ActionInvocationContext getActionInvocationContext() { return actionInvocationContext; }
	public List<String> getSlots() { return slots; }
	
	public IntentActivationContext(String intentName, ActionInvocationContext actionInvocationContext, List<String> slots) {
		this.intentName = intentName;
		this.actionInvocationContext = actionInvocationContext;
		this.slots = slots;
	}
}
