package rottentomatoes;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.apache.http.client.fluent.Request;
import routing.AlexaController;
import routing.AlexaResponse;
import routing.attributes.Slot;
import routing.attributes.Utterances;
import routing.providers.AlexaSessionProvider;
import routing.providers.RequestContextProvider;

import java.io.IOException;

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

        String moviesUri = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/box_office.json?limit=" + Integer.toString(count);
        Movie[] movies = getMovies(moviesUri);

        if (movies == null || movies.length < count) {
            return endSessionResponse("Sorry, I don't know what the top movies are.");
        }

        if (count == 1) {
            return endSessionResponse("The top movie is " + movies[0].title);
        }

        StringBuilder response = new StringBuilder();
        response.append("The top movies are ");
        for (int i = 0; i < count - 1; i++) {
            response.append(movies[i]).append(", ");
        }

        response.append("and ").append(movies[count - 1]);
        return endSessionResponse(response.toString());
    }

    private Movie[] getMovies(String url) {
        url += url.contains("?") ? "&" : "?";
        url += "apikey=" + "7waqfqbprs7pajbz28mqf6vz";

        try {
            String response = Request.Get(url).execute().returnContent().asString();
            Gson gson = new Gson();
            return gson.fromJson(response, RottenTomatoesMovies.class).movies;
        } catch (Exception e) {
            return null;
        }
    }

    private class RottenTomatoesMovies {
        Movie[] movies;
    }

    public class Movie {
        String id;
        String title;
        int year;
    }
}
