import com.amazon.speech.Sdk;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import rottentomatoes.RottenTomatoesSpeechlet;
import routing.SpeechRouter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class Main {

  public static void main(String[] args) throws Exception {
      // Configure logging to output to the console with default level of INFO
      BasicConfigurator.configure();

      System.setProperty(Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "true");

        String envPort = System.getenv("PORT");
        int port = envPort.isEmpty() ? 8000 : Integer.valueOf(envPort);


        // Configure server and its associated servlets
        Server server = new Server();
        ServerConnector serverConnector =
                new ServerConnector(server, new HttpConnectionFactory());
        serverConnector.setPort(port);

        Connector[] connectors = new Connector[1];
        connectors[0] = serverConnector;
        server.setConnectors(connectors);

        Injector injector = Guice.createInjector(new ServletModule());
        
        // , new AbstractModule() {
        //     @Override
        //     protected void configure() {

        //     }
        // }

        final SpeechRouter router = SpeechRouter.create(injector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        server.setHandler(context);
        context.addServlet(new ServletHolder(createServlet(new RottenTomatoesSpeechlet(router))), "/rottentomatoes");
        server.start();
        server.join();
  }
  
  
    private static SpeechletServlet createServlet(final Speechlet speechlet) {
        SpeechletServlet servlet = new SpeechletServlet();
        servlet.setSpeechlet(speechlet);
        return servlet;
    }

}
