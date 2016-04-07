import com.amazon.speech.Sdk;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import com.google.inject.*;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import routing.SpeechRouter;
import routing.providers.AlexaSessionProvider;
import routing.providers.RequestContextProvider;
import routing.servlets.IntentSchemaServlet;
import routing.servlets.RoutingSpeechlet;
import routing.servlets.SampleUtterancesServlet;

public class Main {

    public static void main(String[] args) throws Exception {
        // Configure logging to output to the console with default level of INFO
        BasicConfigurator.configure();

        System.setProperty(Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "true");

        String envPort = System.getenv("PORT");
        int port = envPort == null || envPort.isEmpty() ? 8000 : Integer.valueOf(envPort);


        // Configure server and its associated servlets
        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler();
        context.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                Module services = new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(SpeechRouter.class).toProvider(new Provider<SpeechRouter>() {
                            @Inject Injector injector;

                            @Override
                            public SpeechRouter get() {
                                try {
                                    return SpeechRouter.create(injector, "rottentomatoes");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                        }).in(Singleton.class);
                        bind(AlexaSessionProvider.class).in(RequestScoped.class);
                        bind(RequestContextProvider.class).in(RequestScoped.class);
                        bind(SpeechletServlet.class).toProvider(new Provider<SpeechletServlet>() {
                            @Inject SpeechRouter router;
                            @Inject Injector injector;

                            @Override
                            public SpeechletServlet get() {
                                SpeechletServlet servlet = new SpeechletServlet();
                                servlet.setSpeechlet(new RoutingSpeechlet(router, injector));
                                return servlet;
                            }
                        }).in(Singleton.class);
                    }
                };

                return Guice.createInjector(services, new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        serve("/sample-utterances").with(SampleUtterancesServlet.class);
                        serve("/intent-schema").with(IntentSchemaServlet.class);
                        serve("/rotten-tomatoes").with(SpeechletServlet.class);
                    }
                });
            }
        });
        context.addFilter(GuiceFilter.class, "/*", null);
        context.addServlet(DefaultServlet.class, "/");
        server.setHandler(context);
        server.start();
        server.join();
    }
}
