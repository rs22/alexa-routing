package routing.providers;

public class RequestContextProvider {
    private String possibleUtterance;
    private String intentName;

    public void setPossibleUtterance(String possibleUtterance) {
        this.possibleUtterance = possibleUtterance;
    }

    public String getPossibleUtterance() {
        return this.possibleUtterance;
    }

    public void setIntentName(String intentName) {
        this.intentName = intentName;
    }

    public String getIntentName() {
        return this.intentName;
    }
}
