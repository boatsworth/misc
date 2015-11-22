package ben.misc.resteasy;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import org.jboss.resteasy.util.BasicAuthHelper;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import javax.ws.rs.core.HttpHeaders;
import java.util.concurrent.*;

import static spark.Spark.get;
import static spark.Spark.port;

public class RestEasyClientTest {

    static HelloWorldService service1;
    static HelloWorldService service2;
    static HelloWorldService service3;
    static ResteasyClient client;

    static HelloWorldService service(
            ResteasyClient client, String baseUrl,
            String user, String password) {

        ResteasyWebTarget target = client.target(baseUrl);
        target.register(new BasicAuthentication(user, password));
        return target.proxy(HelloWorldService.class);
    }

    static Route route() {
        return new Route() {
            @Override
            public Object handle(Request request, Response response) {
                String headerVal = request.headers(HttpHeaders.AUTHORIZATION);
                return BasicAuthHelper.parseHeader(headerVal)[1];
            }
        };
    }

    static Runnable runnable(final HelloWorldService s) {
        return new Runnable(){
            @Override
            public void run() {
                javax.ws.rs.core.Response r = s.hello();
                // #readEntity or #close must be called
                // in order to return connection to pool
                String body = r.readEntity(String.class);
                System.out.println(body);
            }
        };
    }

    static void setup() {
        port(4567);
        get("/app1/hello", route());
        get("/app2/hello", route());
        get("/app3/hello", route());

        client = new ResteasyClientBuilder()
                .connectionPoolSize(10)
                .maxPooledPerRoute(2)
                .build();
        service1 = service(client, "http://localhost:4567/app1", "user", "password1");
        service2 = service(client, "http://localhost:4567/app2", "user", "password2");
        service3 = service(client, "http://localhost:4567/app3", "user", "password3");
    }


    public static void main(String[] args) throws Exception {

        setup();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(40);

        for (int i = 0; i < 100; i++) {
            scheduler.schedule(runnable(service1), 0, TimeUnit.SECONDS);
            scheduler.schedule(runnable(service2), 0, TimeUnit.SECONDS);
            scheduler.schedule(runnable(service3), 0, TimeUnit.SECONDS);
        }

        scheduler.shutdown();
        while (!scheduler.awaitTermination(5, TimeUnit.SECONDS));

        // after Sparks stops, ensure Apache HTTP connection manager isn't
        // still running behind the scenes
        Spark.stop();
    }
    


}
