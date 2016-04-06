package routing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionInvocationContext {
	private String controllerName;
	private String actionName;
	private Method actionMethod;
	private List<Method> filterMethods = new ArrayList<Method>();
	
	private Map<String, Class<?>> slots = new HashMap<String, Class<?>>();
	
	public Method getActionMethod() { return actionMethod; }
	public List<Method> getFilterMethods() { return filterMethods; }
	public Map<String, Class<?>> getSlots() { return slots; }
	
	public ActionInvocationContext (String controllerName, String actionName, Method actionMethod)
	{
		this.controllerName = controllerName;
		this.actionName = actionName;
		this.actionMethod = actionMethod;
	}
	
	public void addSlot(String slotName, Class<?> type) throws Exception {
		if (slots.containsKey(slotName)) {
			if (slots.get(slotName) != type)
				throw new Exception(slotName + " slot parameter types on action" + controllerName + "#" + actionName + " do not match");
		} else {
			slots.put(slotName, type);	
		}
	}
	
	public String getSlotType(String slotName) {
		Class<?> slotType = slots.get(slotName);
		
		if (slotType == String.class)
			return "LITERAL";
		if (slotType == int.class)
			return "NUMBER";
		/*
			return "DATE";
		
			return "TIME";
		
			return "DURATION";*/
		return "UNKNOWN";
	}
	
	public void addFilter(Method filterMethod) {
		filterMethods.add(filterMethod);
	}
	
	public String getIntentName(List<String> slots) throws Exception {
		String capitalizedActionName = actionName.substring(0, 1).toUpperCase() + actionName.substring(1);

		if (slots.isEmpty()) {
			return String.format("%1$s%2$sIntent", controllerName, capitalizedActionName);
		} else {
			// Make sure all slots actually exist
			for (String slot : slots) {
				if (this.slots.containsKey(slot) == false)
					throw new Exception("Couldn't resolve slot " + slot + " that was used in utterance");
			}
			
			return String.format("%1$s%2$sWith%3$sIntent", controllerName, capitalizedActionName, String.join("And", slots));
		}
	}
}
