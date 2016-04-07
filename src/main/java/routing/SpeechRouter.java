package routing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Injector;
import org.reflections.Reflections;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import com.google.inject.Singleton;
import routing.attributes.FilterFor;
import routing.attributes.Slot;
import routing.attributes.Utterances;
import routing.providers.RequestContextProvider;

@Singleton
public class SpeechRouter {
	private List<String> sampleUtterances;
	private IntentSchema intentSchema;
	private Map<String, IntentActivationContext> intents;
    private Injector injector;
	
	public List<String> getSampleUtterances() { return sampleUtterances; }
	public IntentSchema getIntentSchema() { return intentSchema; }

	private SpeechRouter(List<String> sampleUtterances, IntentSchema intentSchema, Map<String, IntentActivationContext> intents, Injector injector) {
		this.sampleUtterances = sampleUtterances;
		this.intentSchema = intentSchema;
		this.intents = intents;
        this.injector = injector;
	}
	
	public SpeechletResponse processIntent(String intentName, Intent intent) throws SpeechletException {
		if (intents.containsKey(intentName) == false) {
            throw new SpeechletException("Invalid Intent " + intentName);
        }
		
		IntentActivationContext activation = intents.get(intentName);
		ActionInvocationContext action = activation.getActionInvocationContext();
		
		// Create controller
		AlexaController controller = null;
		Constructor<?> ctor;
		try {
			Class controllerClass = action.getActionMethod().getDeclaringClass();
            controller = (AlexaController)injector.getInstance(controllerClass);
		} catch (Exception e) {
			throw new SpeechletException("Could not create controller instance", e);
		}
		
		// Parse slot values
		Map<String, Object> slotValues = new HashMap<String, Object>();
		for (Entry<String, Class<?>> slot : action.getSlots().entrySet()) {
			if (intent.getSlot(slot.getKey()) == null ||
					intent.getSlot(slot.getKey()).getValue() == null ||
					intent.getSlot(slot.getKey()).getValue().equals("?")) {
				
				// Supply default values for Integer parameters
				if (slot.getValue() == int.class)
					slotValues.put(slot.getKey(), 0);
				
				continue;
			}
				
			if (slot.getValue() == String.class) {
				// Amazon only provides lowercase slot values, so normalize them here for debugging
				slotValues.put(slot.getKey(), intent.getSlot(slot.getKey()).getValue().toLowerCase());
			} else if (slot.getValue() == int.class) {
				slotValues.put(slot.getKey(), Integer.parseInt(intent.getSlot(slot.getKey()).getValue()));
			}
			// TODO: ...
		}
		
		String possibleUtterance = getPossibleUtterance(intentName, slotValues);
		RequestContextProvider contextProvider = this.injector.getInstance(RequestContextProvider.class);
		contextProvider.setPossibleUtterance(possibleUtterance);
		contextProvider.setIntentName(intentName);
		
		// Execute filters
		for (Method filter : action.getFilterMethods()) {
			AlexaController.FilterContext filterContext = controller.new FilterContext();
			
			try {
				List<Object> parameters = collectMethodParameters(filter, slotValues);
				parameters.add(filterContext);
				filter.invoke(controller, parameters.toArray(new Object[parameters.size()]));
				if (filterContext.getResponse() != null)
					return buildSpeechletResponse(filterContext.getResponse(), intentName);
			} catch (Exception e) {
				throw new SpeechletException("Error executing filter " + filter.getName(), e);
			}
		}
		
		// Execute the controller action
		try {
			List<Object> parameters = collectMethodParameters(action.getActionMethod(), slotValues);
			AlexaResponse response = (AlexaResponse)action.getActionMethod()
					.invoke(controller, parameters.toArray(new Object[parameters.size()]));
			return buildSpeechletResponse(response, intentName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SpeechletException("Error executing controller action " + action.getActionMethod().getName(), e);
		}
	}
	
	private SpeechletResponse buildSpeechletResponse(AlexaResponse controllerResponse, String title) {
		// Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle(String.format("Response from Alexa Skill"));
        card.setContent(controllerResponse.getContent());

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(controllerResponse.getContent());

        // Create the speechlet response.
        SpeechletResponse response = new SpeechletResponse();
        response.setShouldEndSession(controllerResponse.getShouldEndInteraction());
        response.setOutputSpeech(speech);
        response.setCard(card);
        return response;
	}
	
	private List<Object> collectMethodParameters(Method method, Map<String, Object> slots) {
		List<Object> result = new LinkedList<Object>();
		Slot slotNames = method.getAnnotation(Slot.class);
		int declaredSlots = slotNames != null && slotNames.value() != null ? slotNames.value().length : 0;
		
		if (declaredSlots == 0)
			return result;
		
		for (String slotName : slotNames.value()){
			result.add(slots.get(slotName));
		}
		return result;
	}
	
	private static Boolean isActionMethod(Method method) {
		if ((method.getModifiers() & Modifier.PUBLIC) == 0)
			return false;
		if (method.getReturnType() != AlexaResponse.class)
			return false;
		if (method.getAnnotation(Utterances.class) == null)
			return false;
		
		return true;
	}
	
	private static Boolean isFilterMethod(Method method) {
		if ((method.getModifiers() & Modifier.PUBLIC) == 0)
			return false;
		if (method.getReturnType() != Void.TYPE)
			return false;
		if (method.getAnnotation(FilterFor.class) == null)
			return false;
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length < 1 || parameterTypes[parameterTypes.length-1] != AlexaController.FilterContext.class)
			return false;
		
		return true;
	}
	
	private static List<String> getSlotsFromUtterance(String utterance) {
		List<String> result = new ArrayList<String>();
		
		Matcher slotNameMatcher = Pattern.compile("\\{([^\\|]+)\\|(?<slot>[a-zA-Z]+)\\}").matcher(utterance);
		while (slotNameMatcher.find())
			result.add(slotNameMatcher.group("slot"));
		
		return result;
	}
	
	private static List<String> getFlattenedUtterances(String utterance) {
		List<String> result = new ArrayList<String>();
		
		Matcher slotMatcher = Pattern.compile("\\{(?<samples>(([^\\|]+);([^\\|]+)))\\|(?<slot>[a-zA-Z]+)\\}").matcher(utterance);
		if (slotMatcher.find()) {
			String[] samples = slotMatcher.group("samples").split(";");
			String slot = slotMatcher.group("slot");
			
			Matcher replacementMatcher = Pattern.compile("\\{((([^\\|]+);([^\\|]+)))\\|([a-zA-Z]+)\\}").matcher(utterance);
			
			for (String sample : samples) {
				replacementMatcher.find(0);
				StringBuffer buffer = new StringBuffer();
				replacementMatcher.appendReplacement(buffer, String.format("{%1$s|%2$s}", sample, slot));
				replacementMatcher.appendTail(buffer);
				result.addAll(getFlattenedUtterances(buffer.toString()));
			}
		} else {
			result.add(utterance);
		}
		
		return result;
	}
	
	private static String getUtteranceWithSlotValues(String utterance, Map<String, Object> slotValues) {
		Matcher slotMatcher = Pattern.compile("\\{([^\\|]+)\\|(?<slot>[a-zA-Z]+)\\}").matcher(utterance);
		if (slotMatcher.find()) {
			String slot = slotMatcher.group("slot");
			
			Matcher replacementMatcher = Pattern.compile("\\{([^\\|]+)\\|([a-zA-Z]+)\\}").matcher(utterance);
			
			if (!slotValues.containsKey(slot) && slotValues.get(slot) == null)
				return null;
			
			replacementMatcher.find(0);
			StringBuffer buffer = new StringBuffer();
			
			if (slotValues.get(slot).getClass() == int.class)
				replacementMatcher.appendReplacement(buffer, String.format("%1$d", slotValues.get(slot)));
			else
				replacementMatcher.appendReplacement(buffer, String.format("%1$s", slotValues.get(slot)));
			
			replacementMatcher.appendTail(buffer);
			return getUtteranceWithSlotValues(buffer.toString(), slotValues);
		} else {
			return utterance;
		}
	}
	
	private String getPossibleUtterance(String intentName, Map<String, Object> slotValues) {
		// Find utterance for intentName
		String intentUtterance = null;
		for (String utterance : sampleUtterances) {
			if (utterance.startsWith(intentName + " ")) {
				intentUtterance = utterance;
				break;
			}
		}
		
		if (intentUtterance == null)
			return null;

		String utteranceWithSlotValues = getUtteranceWithSlotValues(intentUtterance, slotValues);
		if (utteranceWithSlotValues != null && utteranceWithSlotValues.length() >= intentName.length())
			utteranceWithSlotValues = utteranceWithSlotValues.substring(intentName.length() + 1);

		return utteranceWithSlotValues;
	}

	public static SpeechRouter create(Injector injector, String controllerPackage) throws Exception {
		Reflections reflections = new Reflections(controllerPackage);
		Set<Class<? extends AlexaController>> controllers = reflections.getSubTypesOf(AlexaController.class);
		
		List<String> sampleUtterances = new LinkedList<String>();
		
		Map<String, IntentActivationContext> intentActivationContexts = new HashMap<String, IntentActivationContext>();

		for (Class<? extends AlexaController> controller : controllers) {
			if (controller.getSimpleName().endsWith("Controller") == false)
				continue;
			
			Map<String, ActionInvocationContext> invocationContexts = new HashMap<String, ActionInvocationContext>();

			// Find action methods
			for (Method method : controller.getDeclaredMethods()) {
				if (!isActionMethod(method))
					continue;
				
				String controllerName = controller.getSimpleName().substring(0, controller.getSimpleName().length() - "Controller".length());
				String actionName = method.getName();
				
				ActionInvocationContext invocationContext = new ActionInvocationContext(controllerName, actionName, method);
				
				Slot slotNames = method.getAnnotation(Slot.class);
				Class<?>[] parameterTypes = method.getParameterTypes();
				
				// Are there any slots declared?
				int declaredSlots = slotNames != null && slotNames.value() != null ? slotNames.value().length : 0; 
				if (declaredSlots != parameterTypes.length)
					throw new Exception("Declared slots and action method parameters do not match");
				
				for (int i = 0; i < declaredSlots; ++i) {
					invocationContext.addSlot(slotNames.value()[i], parameterTypes[i]);
				}
				
				invocationContexts.put(actionName, invocationContext);
			}
			
			// Find filter methods
			for (Method method : controller.getMethods()) {
				if (!isFilterMethod(method))
					continue;
				
				String[] actionMethodNames = method.getAnnotation(FilterFor.class).value();
				
				List<String> actionMethodNameList = Arrays.asList(actionMethodNames);
				if (actionMethodNameList.contains("*")) {
					actionMethodNameList = new ArrayList<String>();
					// Add all action methods
					for (String action : invocationContexts.keySet())
						actionMethodNameList.add(action);
					
					actionMethodNames = actionMethodNameList.toArray(new String[actionMethodNameList.size()]);
				}
				
				Slot slotNames = method.getAnnotation(Slot.class);
				Class<?>[] parameterTypes = method.getParameterTypes();
				
				// Are there any slots declared?
				int declaredSlots = slotNames != null && slotNames.value() != null ? slotNames.value().length : 0; 
				if (declaredSlots != parameterTypes.length - 1) // Filters have an additional parameter
					throw new Exception("Declared slots and filter method parameters do not match");

				for (String action : actionMethodNames) {
					if (invocationContexts.containsKey(action) == false)
						continue;
					
					ActionInvocationContext invocationContext = invocationContexts.get(action);
					for (int i = 0; i < declaredSlots; ++i) {
						invocationContext.addSlot(slotNames.value()[i], parameterTypes[i]);
					}
					
					invocationContext.addFilter(method);
				}
			}
			
			// Prepare intent schema and sample utterances
			for (ActionInvocationContext invocationContext : invocationContexts.values()) {
				String[] utterances = invocationContext.getActionMethod().getAnnotation(Utterances.class).value();
				
				if (utterances.length == 0) {
					// This should only be true for the welcome intent
					String intentName = invocationContext.getIntentName(new LinkedList<String>());

					if (intentActivationContexts.get(intentName) != null 
							&& intentActivationContexts.get(intentName).getActionInvocationContext() != invocationContext) {
						throw new Exception("Intent " + intentName + " was defined more than once");
					}
					
					intentActivationContexts.put(intentName, new IntentActivationContext(intentName, invocationContext, new LinkedList<String>()));
				}
				
				for (String utterance : utterances) {
					List<String> slots = getSlotsFromUtterance(utterance);
					Collections.sort(slots);
					
					String intentName = invocationContext.getIntentName(slots);
					
					if (intentActivationContexts.get(intentName) != null 
							&& intentActivationContexts.get(intentName).getActionInvocationContext() != invocationContext) {
						throw new Exception("Intent " + intentName + " was defined more than once");
					}
					
					intentActivationContexts.put(intentName, new IntentActivationContext(intentName, invocationContext, slots));
					
					for (String flattenedUtterance : getFlattenedUtterances(utterance)) {
						sampleUtterances.add(intentName + " " + flattenedUtterance);					
					}
				}
			}
		}
		
		// Finalize intent schema
		IntentSchema intentSchema = new IntentSchema();
		List<IntentSchema.IntentSchemaIntent> intents = new LinkedList<IntentSchema.IntentSchemaIntent>();
		for (IntentActivationContext intent : intentActivationContexts.values()) {
			IntentSchema.IntentSchemaIntent schemaIntent = intentSchema.new IntentSchemaIntent();
			schemaIntent.intent = intent.getIntentName();
			
			if (schemaIntent.intent == "CommonIntentsWelcomeIntent")
				continue;
			
			ActionInvocationContext invocationContext = intent.getActionInvocationContext();
			List<IntentSchema.IntentSchemaIntent.IntentSchemaSlot> schemaSlots = new LinkedList<IntentSchema.IntentSchemaIntent.IntentSchemaSlot>();
			for (String slot : intent.getSlots()) {
				IntentSchema.IntentSchemaIntent.IntentSchemaSlot schemaSlot = schemaIntent.new IntentSchemaSlot();
				schemaSlot.name = slot;
				schemaSlot.type = invocationContext.getSlotType(slot);
				schemaSlots.add(schemaSlot);
			}
			schemaIntent.slots = schemaSlots.toArray(new IntentSchema.IntentSchemaIntent.IntentSchemaSlot[schemaSlots.size()]);
			intents.add(schemaIntent);
		}
		intentSchema.intents = intents.toArray(new IntentSchema.IntentSchemaIntent[intents.size()]);

		return new SpeechRouter(sampleUtterances, intentSchema, intentActivationContexts, injector);
	}
}
