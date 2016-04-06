package routing.servlets;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.inject.Injector;
import routing.SpeechRouter;
import routing.providers.AlexaSessionProvider;

public class RoutingSpeechlet implements Speechlet {
    private final SpeechRouter router;
    private final Injector injector;

    public RoutingSpeechlet(SpeechRouter router, Injector injector) {
    
        this.router = router;
        this.injector = injector;
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {

        provideSessionForRequest(session);
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {

        provideSessionForRequest(session);
        return dispatchIntent("CommonIntentsWelcomeIntent", null, session);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {

        provideSessionForRequest(session);
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        return dispatchIntent(intentName, intent, session);
    }

    private SpeechletResponse dispatchIntent(String intentName, Intent intent, Session session) throws SpeechletException {

        provideSessionForRequest(session);
        return router.processIntent(intentName, intent);
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        provideSessionForRequest(session);
    }

    private void provideSessionForRequest(Session session) {
        AlexaSessionProvider sessionProvider = this.injector.getInstance(AlexaSessionProvider.class);
        sessionProvider.setSession(session);
    }
}
