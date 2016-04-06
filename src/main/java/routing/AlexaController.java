package routing;

public class AlexaController {
	public class FilterContext {
		private AlexaResponse response = null;
		public AlexaResponse getResponse() { return response; }
		public void setResponse(AlexaResponse response) { this.response = response; }
	}
	
//	private UserContext session;
//	private ContextService contextService;
	private RequestContext requestContext;
	
//	public final void initialize(UserContext session, ContextService contextService) {
//		this.session = session;
//		this.contextService = contextService;
//	}
	
	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}
	
//	protected UserContext getSession() {
//		return session;
//	}
	
	protected AlexaResponse notFoundResponse(String excuse) {
		return new AlexaResponse(excuse, true);
	}
	
	protected AlexaResponse continueSessionResponse(String response) {
		return new AlexaResponse(response, false);
	}
	
	protected AlexaResponse endSessionResponse(String response) {
		return new AlexaResponse(response, true);
	}

//	protected ContextService getContextService() {
//		return contextService;
//	}
	
	protected RequestContext getRequestContext() {
		return requestContext;
	}
}
