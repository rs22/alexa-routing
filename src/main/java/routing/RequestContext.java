package routing;

public class RequestContext {
	public final String possibleUtterance;
	public final String intent;
	public RequestContext(String possibleUtterance, String intent) {
		this.possibleUtterance = possibleUtterance;
		this.intent = intent;
	}
}
