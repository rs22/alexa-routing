package rottentomatoes;

import routing.AlexaController;
import routing.AlexaResponse;
import routing.attributes.Utterances;

public class CommonIntentsController extends AlexaController {

    @Utterances({})
    public AlexaResponse welcome() {


        return endSessionResponse("Welcome to the demo skill");
    }
}