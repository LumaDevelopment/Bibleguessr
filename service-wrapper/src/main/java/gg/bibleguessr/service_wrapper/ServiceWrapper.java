package gg.bibleguessr.service_wrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main service wrapping library class. This allows a BibleGuessr
 * microservice to communicate over HTTP, RabbitMQ, or plain Java
 * without having to worry about the details of each. This class
 * also handles the configuration of the service wrapper, and
 * provides a simple API for the microservice to use.
 */
public class ServiceWrapper {

  /* ---------- CONSTANTS ---------- */

  /**
   * The name of this class's logger, which
   * is the name of this class.
   */
  public static final String LOGGER_NAME = ServiceWrapper.class.getSimpleName();

  /**
   * Where the service wrapepr config file is located if no
   * other path is specified.
   */
  public static final String DEFAULT_CONFIG_FILE_PATH = "service_wrapper_config.json";

  /* ---------- VARIABLES ---------- */

  // CORE VARIABLES

  /**
   * The logger which all class statements
   * are routed through.
   */
  private final Logger logger;

  /**
   * The location of the config file.
   */
  private final File configFile;

  /**
   * The actual configuration object for the
   * service wrapper, read and written from and
   * to the config file via jackson-databind.
   */
  private ServiceWrapperConfig config;

  /**
   * Keep track of all microservices that are
   * currently running. This is a map from
   * microservice ID to the actual Microservice object.
   */
  private final Map<String, Microservice> runningMicroservices;

  /**
   * Keep track of a request types associated
   * with microservices that are currently running.
   * This is a map from Request class to
   * the Microservice object.
   */
  private final Map<Class<? extends Request>, Microservice> reqTypeToService;

  // WEB OPERATIONS

  /**
   * Vertx instance used for our web-server operations.
   */
  private Vertx vertx;

  /**
   * MainVerticle (Vertx agent), stored so we can add valid paths.
   */
  private MainVerticle mainVerticle;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Creates a ServiceWrapper instance with the default
   * config file location.
   */
  public ServiceWrapper() {
    this(new File(DEFAULT_CONFIG_FILE_PATH));
  }

  /**
   * Creates a ServiceWrapper instance with a custom
   * config file location.
   *
   * @param configFile The location of the config file.
   */
  public ServiceWrapper(File configFile) {

    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.configFile = configFile;
    this.config = null;
    this.runningMicroservices = new HashMap<>();
    this.reqTypeToService = new HashMap<>();

    this.vertx = null;
    this.mainVerticle = null;

  }

  /* ---------- PUBLIC METHODS ---------- */

  /**
   * Finds the appropriate microservice to execute the
   * given request, then passes off the request to
   * that microservice to be executed.
   *
   * @param request The request to execute.
   * @return The response to the request, or null
   * if some sort of error occurs.
   */
  public Response executeRequest(Request request) {

    Microservice executor = reqTypeToService.get(request.getClass());

    if (executor == null) {
      logger.error("Received request that has no matching executor!");
      return null;
    }

    return executor.executeRequest(request);

  }

  /**
   * Gets a clean/user-presentable name for a Microservice.
   * Handled like this just in case we want to modify in the
   * future.
   *
   * @param service The service to get clean name for
   * @return The name of the service, or a null string if
   * the parameter is null.
   */
  public String getCleanServiceName(Microservice service) {

    if (service == null) {
      return "<NULL>";
    }

    return service.getClass().getSimpleName();

  }

  /**
   * Return configuration, for use in other classes.
   *
   * @return The configuration object, containing all
   * configurable aspects of the ServiceWrapper.
   */
  public ServiceWrapperConfig getConfig() {
    return config;
  }

  /**
   * Attempts to load the config file into the
   * config object. If the config file does not
   * exist, attempts to write the default config.
   *
   * @return Whether the program should halt.
   */
  public boolean initializeConfig() {

    if (config != null) {
      // Configuration is already present.
      return false;
    }

    ObjectMapper mapper = new ObjectMapper();

    try {

      // Attempt to read configuration from file
      config = mapper.readValue(configFile, ServiceWrapperConfig.class);

      // If we made it to this point, it was successful, so do not halt
      return false;

    } catch (Exception readConfigEx) {

      // Couldn't read from file, so get default config
      // and attempt to write
      config = ServiceWrapperConfig.getDefault();

      try {

        // Attempt to write default configuration to file
        mapper.writeValue(configFile, config);

        // Could write configuration file, inform user.
        logger.info("Successfully wrote default configuration file. Halting program, modify and restart.");

      } catch (Exception writeConfigEx) {

        // Couldn't write default configuration, inform user.
        logger.error("Error while attempting to write default config file!", writeConfigEx);

      }

    }

    return true;

  }

  /**
   * Initializes the Vertx instance if it is not
   * already initialized.
   *
   * @return Whether the Vertx instance was successfully
   * initialized.
   */
  public boolean initializeVertx() {

    // Make sure Vertx isn't already initialized
    if (this.vertx == null) {
      this.vertx = Vertx.vertx();
    }

    if (this.mainVerticle == null) {

      // Attempt to start the main verticle
      this.mainVerticle = new MainVerticle(this);

      // Concurrency handling
      CountDownLatch latch = new CountDownLatch(1);
      AtomicBoolean success = new AtomicBoolean(false);

      // Deploy verticle
      vertx.deployVerticle(mainVerticle).onComplete(res -> {

        if (res.failed()) {
          logger.error("Error while starting main verticle.", res.cause());
        }

        // Concurrency handling
        success.set(res.succeeded());
        latch.countDown();

      });

      try {
        latch.await();
      } catch (InterruptedException e) {
        logger.error("Interrupted while waiting for main verticle to start.", e);
        return false;
      }

      return success.get();

    }

    return true;

  }

  /**
   * Runs the given microservice. This involves
   * configuration management, hosting with Vert.x
   * and registering request paths (if Vert.x is
   * enabled), and setting up RabbitMQ (if RabbitMQ
   * is enabled).
   *
   * @param service The microservice to run.
   */
  public void run(Microservice service) {

    // Get configuration sorted out
    boolean halt = initializeConfig();

    if (halt) {
      throw new RuntimeException("Configuration file does not exist, see logs for more information.");
    }

    // See if a microservice is already running with this ID
    if (runningMicroservices.containsKey(service.getID())) {
      logger.error("ServiceWrapper was asked to run a microservice, but there is already a microservice with that ID running!");
      return;
    }

    if (config.hostWithVertx()) {

      // Initialize Vertx instance
      boolean vertxLaunched = initializeVertx();

      if (vertxLaunched) {

        // Register all microservice request paths
        // with the main verticle.
        mainVerticle.registerMicroserviceRequests(service);

      } else {
        logger.error("Vertx could not be launched!");
      }

    }

    // TODO RabbitMQ support

    // Now, just keep track that this service
    // is running, and log it
    runningMicroservices.put(service.getID(), service);

    for (Class<? extends Request> requestType : service.getRequestTypes()) {
      reqTypeToService.put(requestType, service);
    }

    logger.info("Successfully started {}!", getCleanServiceName(service));

  }

  /**
   * Shuts down all microservices that the wrapper is
   * currently running. Shuts down Vert.x and RabbitMQ
   * operations.
   */
  public void shutdown() {

    // Shut down all Microservices
    for (Microservice service : runningMicroservices.values()) {
      service.shutdown();
    }

    // Shut down all web hosting.
    if (vertx != null) {
      vertx.close();
    }

    // TODO Shutdown RabbitMQ

  }

  /**
   * Stops the microservice with the given ID. It's request
   * paths are unregistered from the MainVerticle, and
   * it's shutdown() method is called.
   *
   * @param microserviceID The ID of the microservice to stop.
   */
  public void stop(String microserviceID) {

    // Shuts down individual microservice.
    Microservice service = runningMicroservices.remove(microserviceID);

    if (service == null) {
      logger.warn("ServiceWrapper was asked to stop a microservice that is not running.");
      return;
    }

    // Unregister the microservice's paths from
    // MainVerticle
    mainVerticle.unregisterMicroserviceRequests(service);

    // TODO shutdown RabbitMQ ops

    // Remove the request types from reqTypeToService map
    for (Class<? extends Request> requestType : service.getRequestTypes()) {
      reqTypeToService.remove(requestType);
    }

    // Shutdown the microservice and log
    service.shutdown();
    logger.info("Successfully shut down {}.", getCleanServiceName(service));

  }

}
