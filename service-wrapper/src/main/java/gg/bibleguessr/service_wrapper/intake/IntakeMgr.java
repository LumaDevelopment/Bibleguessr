package gg.bibleguessr.service_wrapper.intake;

import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.StatusCode;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import gg.bibleguessr.service_wrapper.ServiceWrapper;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages all classes that accept requests from different
 * communication protocols (HTTPIntake, RabbitMQIntake, etc.)
 */
public class IntakeMgr {

  /* ---------- CONSTANTS ---------- */

  public static final String LOGGER_NAME = IntakeMgr.class.getSimpleName();

  /* ---------- INSTANCE VARIABLES ---------- */

  private final Logger logger;
  private final ServiceWrapper serviceWrapper;
  private final Map<Class<? extends CommsIntake>, CommsIntake> intakes;
  private Vertx vertx;

  /* ---------- CONSTRUCTORS ---------- */

  public IntakeMgr(ServiceWrapper serviceWrapper) {
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.serviceWrapper = serviceWrapper;
    this.intakes = new HashMap<>();
    this.vertx = null;
  }

  /* ---------- METHODS ---------- */

  /**
   * Attempts to deploy the given HTTPIntake as a
   * verticle with Vert.x. Blocks until the
   * deployment finishes successfully or fails.
   *
   * @param intake The HTTPIntake to deploy.
   * @return Whether the deployment was successful.
   */
  private boolean deployHTTPIntake(HTTPIntake intake) {

    // Deploy the verticle with Vert.x

    if (this.vertx == null) {
      this.vertx = Vertx.vertx();
    }

    // Concurrency handling
    CountDownLatch latch = new CountDownLatch(1);
    AtomicBoolean success = new AtomicBoolean(false);

    // Deploy verticle
    vertx.deployVerticle(intake).onComplete(res -> {

      if (res.failed()) {
        logger.error("Error while starting HTTPIntake!", res.cause());
      }

      // Concurrency handling
      success.set(res.succeeded());
      latch.countDown();

    });

    try {
      // Wait for intake to finish launching
      latch.await();
    } catch (InterruptedException e) {
      logger.error("Interrupted while waiting for HTTPIntake to start!", e);
      return false;
    }

    return success.get();

  }

  /**
   * Initializes all intakes that are enabled within
   * the service wrapper's config. This function
   * would be expanded with each new communication
   * protocol added.<br>
   * This function initializes the following
   * intakes (if they are enabled):<br>
   * - HTTPIntake<br>
   * - RabbitMQIntake<br>
   */
  public void initializeIntakes() {

    LinkedList<CommsIntake> toInitialize = new LinkedList<>();

    // HTTP
    if (serviceWrapper.getConfig().hostWithVertx()) {
      toInitialize.push(new HTTPIntake(
        this,
        this.serviceWrapper.getConfig().vertxPort()
      ));
    }

    // RabbitMQ
    if (serviceWrapper.getConfig().hostWithRabbitMQ()) {
      toInitialize.push(new RabbitMQIntake(
        this,
        this.serviceWrapper.getConfig().rabbitMQConfig()
      ));
    }

    // More?

    synchronized (this.intakes) {

      // Initialize each CommsIntake
      while (!toInitialize.isEmpty()) {

        CommsIntake intake = toInitialize.pop();

        if (this.intakes.containsKey(intake.getClass())) {
          // Intake has already been initialized, skip.
          continue;
        }

        // Attempt to initialize the newly created intake
        boolean initSuccessful = intake.initialize();

        if (!initSuccessful) {
          logger.error("Failed to initialize intake: {}", intake.getClass().getSimpleName());
          continue;
        }

        if (intake.getClass().equals(HTTPIntake.class)) {

          // If this is an HTTP intake, there's a special
          // operation we have to do after creating the
          // class

          boolean successfulDeploy = deployHTTPIntake(((HTTPIntake) intake));

          if (!successfulDeploy) {
            // If we couldn't deploy the intake successfully,
            // don't add it to the map
            continue;
          }

        }

        // Store in the map of running intakes
        this.intakes.put(intake.getClass(), intake);

      }

    }

  }

  /**
   * Attempt to execute the request detailed by the given
   * full path (ex. <code>/example-service/example-request</code>)
   * and parameters. The callback will be notified of the
   * result of the request.
   *
   * @param fullPath   The full path of the request.
   * @param parameters The parameters of the request.
   * @param callback   The callback to notify of the result.
   */
  public void receiveRequest(String fullPath, Map<String, String> parameters, CommsCallback callback) {

    if (callback == null || fullPath == null || parameters == null) {
      logger.error("Null parameter(s) passed into receiveRequest()!");
      return;
    }

    // Pull out the microservice ID and request path from the full path
    String[] splitPath = fullPath.split("/");

    if (splitPath.length != 3) {
      logger.error("Received request with invalid path: \"{}\"", fullPath);
      callback.commFailed(StatusCode.HTTP_BAD_URL);
      return;
    }

    String microserviceID = splitPath[1];
    String requestPath = splitPath[2];

    // Convert microservice ID to running service object
    Microservice service = this.serviceWrapper.getRunningMicroservice(microserviceID);

    if (service == null) {
      logger.error("Received request with non-existent microservice ID: \"{}\"", microserviceID);
      callback.commFailed(StatusCode.NO_MICROSERVICE_WITH_ID);
      return;
    }

    // Get the corresponding request object from the
    // request path from the current service object.
    Class<? extends Request> requestClass = service.getRequestTypeFromPath(requestPath);

    if (requestClass == null) {
      logger.error("Received invalid request path \"{}\" for microservice with ID \"{}\".", requestPath, microserviceID);
      callback.commFailed(StatusCode.INVALID_PATH);
      return;
    }

    // Create a request object with the request class
    // and the parameters
    Request request = Request.parse(requestClass, parameters);

    if (request == null) {
      logger.error("Received request with path \"{}\" with invalid parameters!", fullPath);
      callback.commFailed(StatusCode.MALFORMED_REQUEST);
      return;
    }

    // Execute the request and get a response object
    Response response = this.serviceWrapper.executeRequest(request);

    if (response == null) {
      logger.error("Internal error while executing response with type \"{}\".", requestClass.getSimpleName());
      callback.commFailed(StatusCode.INTERNAL_ERROR);
      return;
    }

    // Convert response to string and notify the callback
    callback.commSucceeded(response.toJSONString());

  }

  public void shutdown() {

    // Shut down Vert.x
    vertx.close();

    // Shut down all CommsIntake objects and remove
    // them from the list of available CommsIntakes

    synchronized (this.intakes) {

      for (CommsIntake intake : this.intakes.values()) {
        intake.shutdown();
      }

      this.intakes.clear();

    }

  }

}
