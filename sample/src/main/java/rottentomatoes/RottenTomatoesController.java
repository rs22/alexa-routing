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
import java.util.*;
import java.util.stream.Collectors;

public class RottenTomatoesController extends AlexaController {

    private final RequestContextProvider requestContext;
    private final AlexaSessionProvider session;

    private Movie[] movies;

    @Inject
    public RottenTomatoesController(RequestContextProvider requestContext, AlexaSessionProvider session) {
        this.requestContext = requestContext;
        this.session = session;
    }

    @FilterFor({"topMovies", "topActors", "movieWithActor"})
    @Slot({"Count"})
    public void loadMovies(int count, FilterContext context) {
        // 0 means the user didn't specify a number (he's allowed to do that)
        if (count == 0) {
            count = 10;
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

        return endSessionResponse("The top movies are " +
                joinWordsWithCommasAndAnd(
                        Arrays.asList(this.movies).stream().map(m -> m.title).collect(Collectors.toList())));

    }

    @Utterances({
        "who appears in the most movies",
        "which actor appears in the most movies",
        "who plays in the most of the top {ten;twenty;thirty;fifty;hundred|Count} movies",
    })
    public AlexaResponse topActors() {
        Map<String, List<String>> actorAppearances = getActorAppearances(this.movies);
        String[] actors = actorAppearances.keySet().toArray(new String[actorAppearances.keySet().size()]);

        if (actors.length == 0) {
            return endSessionResponse("Apparently there's not a single actor in the top movies.");
        }

        String mostAppearances = "";
        Integer previousBest = 0;

        for (int i = 1; i < actors.length; i++) {
            int appearances = actorAppearances.get(actors[i]).size();
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

    @Utterances({
        "which is the {Bradley Cooper;Jennifer Lawrence;Ben Affleck;Amy Adams|Actor} movie"
    })
    @Slot({"Actor"})
    public AlexaResponse movieWithActor(String actor) {
        if (actor == null) {
            return endSessionResponse("I don't know which actor you mean.");
        }

        Map<String, List<String>> actorAppearances = getActorAppearances(this.movies);
        String[] actors = actorAppearances.keySet().toArray(new String[actorAppearances.keySet().size()]);

        for (int i = 0; i < actors.length; i++) {
            if (actor.equals(actors[i].toLowerCase())) {
                List<String> appearances = actorAppearances.get(actors[i]);
                return endSessionResponse(capitalizeWords(actor) + " appears in " + joinWordsWithCommasAndAnd(appearances));
            }
        }

        return endSessionResponse("Sorry, I don't know a movie with this actor");
    }

    private String capitalizeWords(String text) {
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty())
                continue;

            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        }

        return String.join(" ", words);
    }

    private String joinWordsWithCommasAndAnd(List<String> words) {
        StringBuilder result = new StringBuilder();
        int wc = words.size();

        if (wc == 0)
            return "";
        if (wc == 1)
            return words.get(0);

        for (int i = 0; i < wc - 2; i++) {
            result.append(words.get(i)).append(", ");
        }
        result.append(words.get(wc - 2)).append(" and ").append(words.get(wc - 1));
        return result.toString();
    }

    private Map<String, List<String>> getActorAppearances(Movie[] movies) {
        Map<String, List<String>> actorAppearances = new HashMap<>();

        for (Movie movie : movies) {
            if (movie.abridged_cast == null)
                continue;

            for (Actor actor : movie.abridged_cast) {
                List<String> appearances = new ArrayList<String>();

                if (actorAppearances.containsKey(actor.name))
                    appearances = actorAppearances.get(actor.name);

                appearances.add(movie.title);
                actorAppearances.put(actor.name, appearances);
            }
        }
        return actorAppearances;
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
