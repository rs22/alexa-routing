package routing.servlets;

import com.google.inject.Inject;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import routing.SpeechRouter;

public class RoutingSpeechlet implements Speechlet {
    private final SpeechRouter router;

    public RoutingSpeechlet(SpeechRouter router) {
    
        this.router = router;
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
    
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        
        return dispatchIntent("CommonIntentsWelcomeIntent", null, session);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        return dispatchIntent(intentName, intent, session);
    }

    private SpeechletResponse dispatchIntent(String intentName, Intent intent, Session session) throws SpeechletException {
        
        return router.processIntent(intentName, intent);
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        
    }
}
