package routing.providers;

import com.amazon.speech.speechlet.Session;

public class AlexaSessionProvider {
    private Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }
}
