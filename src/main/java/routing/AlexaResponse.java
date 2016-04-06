package routing;

public class AlexaResponse {
	private Boolean shouldEndInteraction;
	private String content;
	public Boolean getShouldEndInteraction() { return shouldEndInteraction; }
	public void setShouldEndInteraction(Boolean shouldEndInteraction) {	this.shouldEndInteraction = shouldEndInteraction; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	
	public AlexaResponse(String content, Boolean shouldEndInteraction) {
		this.shouldEndInteraction = shouldEndInteraction;
		this.content = content;
	}
}
