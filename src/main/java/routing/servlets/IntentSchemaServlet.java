package routing.servlets;

import com.google.gson.Gson;
import routing.SpeechRouter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by I848587 on 4/5/2016.
 */
public class IntentSchemaServlet extends HttpServlet {
    private final SpeechRouter router;

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
