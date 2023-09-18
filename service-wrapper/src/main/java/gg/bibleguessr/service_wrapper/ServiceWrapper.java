package gg.bibleguessr.service_wrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main service wrapping library class. This allows a Bibleguessr
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
   * Vertx instance used for our web-server operations.
   */
  private Vertx vertx;

  /**
   * MainVerticle (Vertx agent) so we can add valid
   * paths.
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

    this.configFile = configFile;
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);

    this.config = null;
    this.vertx = null;
    this.mainVerticle = null;

  }

  /* ---------- PUBLIC METHODS ---------- */

  public Response executeRequest(Request request) {
    // TODO execute request
    return null;
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

  public void run(Microservice service) {

    // Get configuration sorted out
    boolean halt = initializeConfig();

    if (halt) {
      throw new RuntimeException("Configuration file does not exist, see logs for more information.");
    }

    if (config.hostWithVertx()) {

      // Initialize Vertx instance
      boolean vertxLaunched = initializeVertx();

      if (vertxLaunched) {

        // TODO add all microservice request paths to MainVerticle

      } else {
        logger.error("Vertx could not be launched!");
      }

    }

    // TODO RabbitMQ support

  }

  public void shutdown() {

    if (vertx != null) {
      vertx.close();
    }

    // TODO Shutdown RabbitMQ

  }

}
