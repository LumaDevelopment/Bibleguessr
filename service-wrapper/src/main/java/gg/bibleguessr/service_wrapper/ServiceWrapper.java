package gg.bibleguessr.service_wrapper;

import gg.bibleguessr.backend_utils.BibleguessrUtilities;
import gg.bibleguessr.service_wrapper.intake.IntakeMgr;
import gg.bibleguessr.service_wrapper.self_service.SelfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

  /**
   * Accepts requests from all communication protocols
   * (HTTP, RabbitMQ).
   */
  private final IntakeMgr intakeMgr;

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
    this(configFile, null);
  }

  /**
   * Creates a ServiceWrapper instance with a custom
   * configuration object.
   *
   * @param config The configuration object.
   */
  public ServiceWrapper(ServiceWrapperConfig config) {
    this(null, config);
  }

  /**
   * Internal constructor that is called by all the
   * public constructors. Supports setting a custom
   * configuration file, but also supports setting
   * a custom configuration object directly.
   *
   * @param configFile The location of the config file.
   * @param config     The configuration object.
   */
  private ServiceWrapper(File configFile, ServiceWrapperConfig config) {

    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.configFile = configFile;
    this.config = config;
    this.runningMicroservices = new HashMap<>();
    this.reqTypeToService = new HashMap<>();

    this.intakeMgr = new IntakeMgr(this);

    // Run the self-service of the Service Wrapper
    run(new SelfService(this));

    logger.info("Service Wrapper initialized!");

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

    if (request == null) {
      logger.error("Null request given to execute!");
      return null;
    }

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
  public static String getCleanServiceName(Microservice service) {

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
   * Gets the class that manages all request intake
   * classes, which by proxy means that it manages
   * all communication protocols.
   *
   * @return The IntakeMgr instance.
   */
  public IntakeMgr getIntakeMgr() {
    return intakeMgr;
  }

  /**
   * If a microservice with the given ID is running,
   * return it.
   *
   * @param microserviceID The ID of the microservice
   * @return The microservice with the given ID, or
   * null if no such microservice is running.
   */
  public Microservice getRunningMicroservice(String microserviceID) {
    return runningMicroservices.get(microserviceID);
  }

  /**
   * Returns an unmodifiable set of currently running
   * microservices.
   *
   * @return The currently running microservices.
   */
  public Collection<Microservice> getRunningMicroservices() {
    return runningMicroservices
      .values()
      .stream()
      .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Checks if the config object already exists,
   * and if so, returns it. Otherwise, attempts
   * to read the config file and create the
   * config object. Additionally, if the config
   * file doesn't exist and the config class has
   * a getDefault() method, it will be called
   * and the default config will be written to
   * the config file.
   *
   * @return Whether the config now is set
   * correctly.
   */
  public boolean initializeConfig() {

    if (config != null) {
      return true;
    }

    // Attempt to create the config.
    config = BibleguessrUtilities.getConfigObjFromFile(configFile, ServiceWrapperConfig.class);

    return config != null;

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

    // Attempt to get configuration
    if (!initializeConfig()) {
      throw new RuntimeException("Configuration file does not exist, see logs for more information.");
    }

    // See if a microservice is already running with this ID
    if (runningMicroservices.containsKey(service.getID())) {
      logger.error("ServiceWrapper was asked to run a microservice, but there is already a microservice with that ID running!");
      return;
    }

    // Initialize all intakes (HTTP, RabbitMQ, etc.) that
    // are enabled in the configuration
    intakeMgr.initializeIntakes();

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

    // Shut down the intake manager and all
    // of its intakes.
    intakeMgr.shutdown();

    // Shut down all Microservices
    for (Microservice service : runningMicroservices.values()) {
      service.shutdown();
    }

    logger.info("Shut down Service Wrapper!");

  }

  /**
   * Stops the microservice with the given ID. It's request
   * paths are unregistered from the HTTPIntake, and
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

    // Remove the request types from reqTypeToService map
    for (Class<? extends Request> requestType : service.getRequestTypes()) {
      reqTypeToService.remove(requestType);
    }

    // Shutdown the microservice and log
    service.shutdown();
    logger.info("Successfully shut down {}.", getCleanServiceName(service));

  }

}
