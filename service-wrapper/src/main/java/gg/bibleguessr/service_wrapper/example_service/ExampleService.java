package gg.bibleguessr.service_wrapper.example_service;

import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An example service to demonstrate how to use the Service
 * Wrapper. Takes in a "msg" string and returns if the
 * message length is evenly divisible by 2.
 */
public class ExampleService extends Microservice {

  /* ---------- CONSTANTS ---------- */

  /**
   * The "tag" or identifier behind all logging statements
   * made by this class. Typically just the name of the class.
   */
  public static final String LOGGER_NAME = ExampleService.class.getSimpleName();

  /**
   * The unique identifier of this microservice. Should
   * not conflict with any other microservice being
   * run by the service wrapper.
   */
  public static final String ID = "example-service";

  /**
   * Logging object.
   */
  private final Logger logger;

  /**
   * Allows use with testing classes.
   */
  private boolean hasBeenStopped = false;

  /* ---------- CONSTRUCTOR ---------- */

  /**
   * Default constructor. Unlike Request, no specific
   * constructor is needed for a Microservice. However,
   * since there's no other information that needs to
   * be passed through, we simply pass on the ID to
   * the super constructor, and let that be that.
   */
  public ExampleService() {
    super(ID);
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
  }

  /* ---------- METHODS ---------- */

  /**
   * Execute the given request. This is put into a separate
   * method for executeRequest(), because if you have
   * multiple request types, you'll probably want to pass
   * each type off to a different method.
   *
   * @param request The request to execute
   * @return The response to the request
   */
  private Response executeExampleRequest(ExampleRequest request) {

    boolean lengthDivisibleBy2 = request.getMsg().length() % 2 == 0;

    // Put the response content into a String to
    // String map. In correspondence to what is
    // detailed in RequestResponseSpecifications.md
    Map<String, String> content = new HashMap<>();
    content.put("lengthDivisibleBy2", Boolean.toString(lengthDivisibleBy2));

    logger.debug("Executed ExampleRequest. Message: \"{}\". Length divisible by 2: {}",
      request.getMsg(), lengthDivisibleBy2);

    // If request is not identifiable, then getUUID()
    // will return null, and Response constructor
    // can handle that just fine
    return new Response(content, request.getUUID());

  }

  /**
   * Think of this as a request type
   * switch case, except you cast the request
   * to the actual request type. This method is
   * called by ServiceWrapper and should only
   * be called with Request types that this
   * service can actually execute.
   *
   * @param request The request to execute.
   * @return The response to the request, or
   * null if the type of request is unknown.
   */
  @Override
  public Response executeRequest(Request request) {

    if (request instanceof ExampleRequest exampleRequest) {
      return executeExampleRequest(exampleRequest);
    } else {
      // Unknown type of request
      logger.error("Received Request of unknown type: {}", request.getClass().getSimpleName());
      return null;
    }

  }

  /**
   * Whether this microservice has been fully stopped.
   *
   * @return Whether this microservice has been fully stopped.
   */
  public boolean hasBeenStopped() {
    return hasBeenStopped;
  }

  /**
   * Inform ServiceWrapper all the types of requests
   * we can handle. For this service, only one.
   */
  @Override
  public void initializeRequestTypesMap() {
    initializeRequestType(ExampleRequest.class);
  }

  /**
   * Shutdown the service. Called when this service
   * is shut down specifically or when the Service
   * Wrapper as a whole is shut down. For this
   * service, nothing to do because it's so basic.
   */
  @Override
  public void shutdown() {
    hasBeenStopped = true;
    logger.info("Example Service is shutting down :(");
  }

}
