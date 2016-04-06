package routing.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import routing.SpeechRouter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Singleton
public class IntentSchemaServlet extends HttpServlet {
    private final SpeechRouter router;

    @Inject
    public IntentSchemaServlet(SpeechRouter router) {
        super();
        this.router = router;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        response.getWriter().append(gson.toJson(router.getIntentSchema()));
        response.setContentType("application/json");
    }
}
