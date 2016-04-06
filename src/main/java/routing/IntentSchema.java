package routing;

public class IntentSchema {
	public class IntentSchemaIntent {
		public class IntentSchemaSlot {
			public String name;
			public String type;
		}
		
		public String intent;
		public IntentSchemaSlot[] slots;
	}
	
	public IntentSchemaIntent[] intents;
}