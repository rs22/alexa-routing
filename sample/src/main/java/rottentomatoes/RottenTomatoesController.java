package rottentomatoes;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import routing.AlexaController;
import routing.AlexaResponse;
import routing.attributes.Slot;
import routing.attributes.Utterances;
import routing.providers.AlexaSessionProvider;
import routing.providers.RequestContextProvider;

public class RottenTomatoesController extends AlexaController {

    private final RequestContextProvider requestContext;
    private final AlexaSessionProvider session;

    @Inject
    public RottenTomatoesController(RequestContextProvider requestContext, AlexaSessionProvider session) {
        this.requestContext = requestContext;
        this.session = session;
    }

    @Utterances({
        "what are the top {one;two;three;four;five;ten;fifteen;twenty|Count} movies"
    })
    @Slot({"Count"})
    public AlexaResponse topMovies(int count) {

        String utterance = requestContext.getPossibleUtterance();
        if (utterance != null && !utterance.isEmpty()) {
            return endSessionResponse("You asked me '" + utterance + "' but I don't know the answer");
        }

        return endSessionResponse("I don't know what the top movies are.");
    }

}
