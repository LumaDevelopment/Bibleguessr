package gg.bibleguessr.service_wrapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
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
   * ServiceWrapper instance to pass off requests to,
   * get clean service names, access configuration, etc.
   */
  private final ServiceWrapper serviceWrapper;

  /**
   * Map between valid paths and their corresponding request classes.
   */
  private final HashMap<String, Class<? extends Request>> paths;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Default constructor. Service Wrapper is passed in
   * for config access, request execution, etc.
   *
   * @param serviceWrapper the ServiceWrapper instance
   */
  public MainVerticle(ServiceWrapper serviceWrapper) {

    if (serviceWrapper == null) {
      throw new RuntimeException("ServiceWrapper passed into MainVerticle cannot be null!");
    }

    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.serviceWrapper = serviceWrapper;
    this.paths = new HashMap<>();

  }

  /* ---------- METHODS ---------- */

  /**
   * Gets the path of the given the request, belonging to
   * the given service on the Vert.x web server.
   *
   * @param service The service that executes the request.
   * @param request The request to be executed.
   * @return The path on the web server to submit requests of this type.
   */
  public String getRequestPath(Microservice service, Request request) {

    // Make sure vital components are there
    if (service == null || request == null) {
      return null;
    }

    return "/" + service.getID() + "/" + request.getRequestPath();

  }

  /**
   * Returns whether the given path belongs to the given
   * service. If either of the parameters are null, then
   * the function defaults to return false.
   *
   * @param path    The path to check.
   * @param service The service that may correspond to the path.
   * @return Whether the path belongs to the given service.
   */
  public boolean pathBelongsToService(String path, Microservice service) {

    // Make sure vital components are there
    if (path == null || service == null) {
      return false;
    }

    return path.startsWith("/" + service.getID());

  }

  /**
   * Register all requests from the given microservice with this verticle.
   *
   * @param service The microservice to register requests from.
   */
  public void registerMicroserviceRequests(Microservice service) {

    if (service == null) {
      return;
    }

    for (Class<? extends Request> requestClazz : service.getRequestTypes()) {

      try {

        // Create new instance of the request so that we
        // can obtain its ID
        Request request = requestClazz.getDeclaredConstructor().newInstance();

        // Construct the appropriate request path using microservice ID
        // and request ID
        paths.put(getRequestPath(service, request), requestClazz);

      } catch (Exception e) {
        // Log failure, but don't need to skip all the other requests
        logger.error("Failed to register request type " + requestClazz.getSimpleName() + "!", e);
      }

    }

    logger.debug("Paths for microservice {} registered.", serviceWrapper.getCleanServiceName(service));

  }

  /**
   * Starts this Verticle on the port defined in the configuration.
   * Handles incoming requests and responds with a 404, malformed
   * request error, or a request response.
   *
   * @param startPromise a promise which should be called when verticle start-up is complete.
   */
  @Override
  public void start(Promise<Void> startPromise) {

    int port = serviceWrapper.getConfig().vertxPort();

    vertx.createHttpServer().requestHandler(req -> {

      Class<? extends Request> requestClass = paths.get(req.path());

      if (requestClass == null) {
        // Unknown path, 404
        throwPathNotFound(req);
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

      // We somehow failed to execute the request.
      if (response == null) {
        throwInternalServerError(req);
        return;
      }

      // Deliver response to client as JSON
      req.response()
        .putHeader("content-type", "application/json")
        .end(response.toJSONString());

    }).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        logger.info("Vert.x launched successfully on port {}!", port);
      } else {
        // Failure will be logged in ServiceWrapper
        startPromise.fail(http.cause());
      }
    });

  }

  /**
   * Sends a 500 code to the client, indicating an internal
   * server error occurred.
   *
   * @param request The request to respond to with the 500.
   */
  public void throwInternalServerError(HttpServerRequest request) {

    if (request == null) {
      return;
    }

    request.response().setStatusCode(500).end();

  }

  /**
   * Sends a 400 code to the client, indicating the request
   * they attempted to send was malformed.
   *
   * @param request The request to respond to with the 400.
   */
  public void throwMalformedRequestError(HttpServerRequest request) {

    if (request == null) {
      return;
    }

    request.response().setStatusCode(400).end();

  }

  /**
   * Sends a 404 code to the client, indicating the path
   * they attempted to access is not registered.
   *
   * @param request The request to respond to with the 404.
   */
  public void throwPathNotFound(HttpServerRequest request) {

    if (request == null) {
      return;
    }

    request.response().setStatusCode(404).end();

  }

  /**
   * Unregisters all paths associated with the
   * given microservice.
   *
   * @param microservice The microservice that owns all
   *                     paths to be removed.
   */
  public void unregisterMicroserviceRequests(Microservice microservice) {

    if (microservice == null) {
      return;
    }

    // Store all paths in a way that doesn't reflect the map.
    LinkedList<String> pathStrings = new LinkedList<>(paths.keySet());

    for (String path : pathStrings) {

      if (!pathBelongsToService(path, microservice)) {
        // Path doesn't belong to the microservice,
        // so we're not interested
        continue;
      }

      // Path does belong to microservice, remove
      paths.remove(path);

    }

  }

}
