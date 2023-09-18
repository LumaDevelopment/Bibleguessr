package gg.bibleguessr.service_wrapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles exposing microservices to the World Wide Web.
 * Monitors valid and invalid paths.
 * Parses incoming requests and passes them off to ServiceWrapper.
 */
public class MainVerticle extends AbstractVerticle {

  /* ---------- CONSTANTS ---------- */

  /**
   * The name of this class's logger, which
   * is the name of this class.
   */
  public static final String LOGGER_NAME = MainVerticle.class.getSimpleName();

  /* ---------- VARIABLES ---------- */

  /**
   * The logger for this class.
   */
  private final Logger logger;

  /**
   * ServiceWrapper instance to pass off requests to.
   */
  private final ServiceWrapper serviceWrapper;

  /**
   * Map between valid paths and their corresponding request classes.
   */
  private final HashMap<String, Class<? extends Request>> paths;

  /* ---------- CONSTRUCTORS ---------- */

  public MainVerticle(ServiceWrapper serviceWrapper) {
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.serviceWrapper = serviceWrapper;
    this.paths = new HashMap<>();
  }

  /* ---------- METHODS ---------- */

  /**
   * Register all requests from the given microservice with this vericle.
   *
   * @param microservice The microservice to register requests from.
   */
  public void registerMicroserviceRequests(Microservice microservice) {

    if (microservice == null) {
      return;
    }

    // TODO register requests

  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {

      Class<? extends Request> requestClass = paths.get(req.path());

      if (requestClass == null) {
        // Unknown path, 404
        throw404(req);
        return;
      }

      // Load parameters into map
      Map<String, String> paramMap = new HashMap<>();
      req.params().forEach(entry -> {
        paramMap.put(entry.getKey(), entry.getValue());
      });

      // Attempt to parse the request
      Request request = Request.parse(requestClass, paramMap);

      // If the request is null, it was malformed
      // and unable to be parsed
      if (request == null) {
        throwMalformedRequestError(req);
        return;
      }

      // Execute the request
      Response response = serviceWrapper.executeRequest(request);

      // TODO Deliver response to the client, probably as JSON
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");

    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });

  }

  /**
   * Sends a 404 code to the client.
   *
   * @param request The request to respond to with the 404.
   */
  public void throw404(HttpServerRequest request) {

    if (request == null) {
      return;
    }

    request.response().setStatusCode(404).end();

  }

  public void throwMalformedRequestError(HttpServerRequest request) {

    // TODO implement
    return;

  }

}
