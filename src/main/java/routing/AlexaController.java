package routing;

public class AlexaController {
	public class FilterContext {
		private AlexaResponse response = null;
		public AlexaResponse getResponse() { return response; }
		public void setResponse(AlexaResponse response) { this.response = response; }
	}

	protected AlexaResponse continueSessionResponse(String response) {
		return new AlexaResponse(response, false);
	}
	
	protected AlexaResponse endSessionResponse(String response) {
		return new AlexaResponse(response, true);
	}
}
