package routing.servlets;

import routing.SpeechRouter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Singleton
public class SampleUtterancesServlet extends HttpServlet {
    private final SpeechRouter router;

    @Inject
    public SampleUtterancesServlet(SpeechRouter router) {
        super();
        this.router = router;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        for (String utterance : router.getSampleUtterances()){
            writer.append(utterance).append("\n");
        }
        response.setContentType("text/plain");
    }
}
