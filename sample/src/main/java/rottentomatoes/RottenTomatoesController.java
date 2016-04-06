package rottentomatoes;

import routing.AlexaController;
import routing.AlexaResponse;
import routing.attributes.Slot;
import routing.attributes.Utterances;

public class RottenTomatoesController extends AlexaController {

    @Utterances({
        "what are the top {one;two;three;four;five;ten;fifteen;twenty|Count} movies"
    })
    @Slot({"Count"})
    public AlexaResponse TopMovies(int count) {

        return endSessionResponse("I don't know what the top movies are");
    }

}
