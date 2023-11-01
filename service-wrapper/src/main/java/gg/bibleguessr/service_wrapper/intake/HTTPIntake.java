package gg.bibleguessr.service_wrapper.intake;

import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.StatusCode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles exposing microservices to the World Wide Web.
 * Monitors valid and invalid paths.
 * Parses incoming requests and passes them off to ServiceWrapper.
 */
public class HTTPIntake extends AbstractVerticle implements CommsIntake {

  /* ---------- CONSTANTS ---------- */

  /**
   * The name of this class's logger, which
   * is the name of this class.
   */
  public static final String LOGGER_NAME = HTTPIntake.class.getSimpleName();

  /* ---------- VARIABLES ---------- */

  /**
   * The logger for this class.
   */
  private final Logger logger;

  /**
   * IntakeMgr to pass off requests to. Does some
   * sanity checking on requests, attempts to execute
   * them, and then calls the CommsCallback with the
   * appropriate success/failure information.
   */
  private final IntakeMgr intakeMgr;

  /**
   * The API key to validate with incoming HTTP requests.
   * A blank key means we don't check for an API key.
   */
  private final String apiKey;

  /**
   * The port that this web server should
   * be hosted on.
   */
  private final int port;

  /**
   * The origins that are allowed to make CORS
   * requests to the Service Wrapper.
   */
  private final List<String> allowedCorsOrigins;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Default constructor. Service Wrapper is passed in
   * for config access, request execution, etc.
   *
   * @param intakeMgr          The IntakeMgr instance.
   * @param apiKey             The API key required to authenticate HTTP
   *                           requests. If empty, do not validate or
   *                           check for key.
   * @param port               The port this web server should be hosted on.
   * @param allowedCorsOrigins The origins that are allowed to make CORS
   *                           requests to the Service Wrapper.
   */
  public HTTPIntake(IntakeMgr intakeMgr, String apiKey, int port, List<String> allowedCorsOrigins) {

    if (intakeMgr == null) {
      throw new RuntimeException("IntakeMgr passed into HTTPIntake cannot be null!");
    }

    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.intakeMgr = intakeMgr;
    this.apiKey = apiKey;
    this.port = port;
    this.allowedCorsOrigins = allowedCorsOrigins;

  }

  /* ---------- METHODS ---------- */

  /**
   * HTTPIntake's true initialization is done when
   * we attempt to deploy it using Vert.x, so
   * this method automatically returns true.
   *
   * @return <code>true</code>
   */
  @Override
  public boolean initialize() {
    return true;
  }

  /**
   * Vert.x is shut off in IntakeMgr, so nothing to be
   * done here.
   */
  @Override
  public void shutdown() {
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

    // Create new HttpServer and Router
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    // Add a route to avoid CORS issues
    router.route().handler(CorsHandler.create()
      .addOrigins(allowedCorsOrigins)
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedHeader("Access-Control-Request-Method")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Content-Type")
    );

    vertx.createHttpServer().requestHandler(req -> {

      // Load parameters into map
      Map<String, String> parameters = new HashMap<>();
      req.params().forEach(entry -> parameters.put(entry.getKey(), entry.getValue()));

      // Check for API key
      if (!this.apiKey.isEmpty()) {

        String givenAPIKey = parameters.get("apiKey");

        if (givenAPIKey == null || !givenAPIKey.equals(this.apiKey)) {
          throwErrorCode(req, StatusCode.BAD_API_KEY);
          return;
        }

      }

      // Decide what to do based on the response
      CommsCallback callback = new CommsCallback() {

        @Override
        public void onSuccess(String content) {
          // Deliver JSON string as response to request
          req.response()
            .putHeader("content-type", "application/json")
            .end(content);
        }

        @Override
        public void onFailure(StatusCode errorCode) {
          // Throw response code
          throwErrorCode(req, errorCode);
        }

      };

      // Attempt to execute the request
      intakeMgr.receiveRequest(req.path(), parameters, callback);

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
   * Ends a request/session that a client has open with us
   * with the given error code.
   *
   * @param request   The request to respond to.
   * @param errorCode The error code to respond with.
   */
  public void throwErrorCode(HttpServerRequest request, StatusCode errorCode) {

    if (request == null) {
      return;
    }

    request.response().setStatusCode(errorCode.getStatusCode()).end();

  }

}
