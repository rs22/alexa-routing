package rottentomatoes;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.apache.http.client.fluent.Request;
import routing.AlexaController;
import routing.AlexaResponse;
import routing.attributes.FilterFor;
import routing.attributes.Slot;
import routing.attributes.Utterances;
import routing.providers.AlexaSessionProvider;
import routing.providers.RequestContextProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RottenTomatoesController extends AlexaController {

    private final RequestContextProvider requestContext;
    private final AlexaSessionProvider session;

    private Movie[] movies;

    @Inject
    public RottenTomatoesController(RequestContextProvider requestContext, AlexaSessionProvider session) {
        this.requestContext = requestContext;
        this.session = session;
    }

    @FilterFor({"topMovies", "topActors"})
    @Slot({"Count"})
    public void LoadMovies(int count, FilterContext context) {
        // 0 means the user didn't specify a number (he's allowed to do that)
        if (count == 0) {
            count = 5;
        }

        String moviesUri = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/box_office.json?limit=" + Integer.toString(count);
        Movie[] movies = getMovies(moviesUri);

        if (movies == null || movies.length < count) {
            context.setResponse(endSessionResponse("Sorry, I don't know what the top movies are."));
        }

        this.movies = movies;
    }

    @Utterances({
        "what are the top selling movies",
        "what are the top {one;two;three;four;five;ten;fifteen;twenty|Count} movies",
    })
    @Slot({"Count"})
    public AlexaResponse topMovies(int count) {
        // Count is bound both here and in the filter, although it's not being used here

        if (this.movies.length == 1) {
            return endSessionResponse("The top movie is " + this.movies[0].title);
        }

        StringBuilder response = new StringBuilder();
        response.append("The top movies are ");
        for (int i = 0; i < this.movies.length - 1; i++) {
            response.append(this.movies[i].title).append(", ");
        }

        response.append("and ").append(this.movies[count - 1].title);
        return endSessionResponse(response.toString());
    }

    @Utterances({
        "who appears in the most movies",
        "which actor appears in the most movies",
        "who plays in the most of the top {ten;twenty;thirty;fifty;hundred|Count} movies",
    })
    public AlexaResponse topActors() {
        Map<String, Integer> actorAppearances = new HashMap<String, Integer>();

        for (Movie movie : this.movies) {
            if (movie.abridged_cast == null)
                continue;

            for (Actor actor : movie.abridged_cast) {
                Integer appearances = 0;

                if (actorAppearances.containsKey(actor.name))
                    appearances = actorAppearances.get(actor.name);

                appearances += 1;
                actorAppearances.put(actor.name, appearances);
            }
        }

        String[] actors = actorAppearances.values().toArray(new String[0]);
        if (actors.length == 0) {
            return endSessionResponse("Apparently there's not a single actor in the top movies.");
        }

        String mostAppearances = "";
        Integer previousBest = 0;

        for (int i = 1; i < actors.length; i++) {
            int appearances = actorAppearances.get(actors[i]);
            if (appearances > previousBest) {
                mostAppearances = actors[i];
                previousBest = appearances;
            }
        }

        if (previousBest == 1) {
            return endSessionResponse("There's no actor who played in more than one movie");
        }

        return endSessionResponse(mostAppearances + " played in " + previousBest + " movies.");
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
        Actor[] abridged_cast;
    }

    public class Actor {
        String name;
    }
}
