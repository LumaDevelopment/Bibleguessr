package gg.bibleguessr.service_wrapper.example_service;

import gg.bibleguessr.service_wrapper.Request;

import java.util.Map;

/**
 * An example request type, specified in <code>RequestResponseSpecifications.md</code>.
 */
public class ExampleRequest extends Request {

  /* ---------- CONSTANTS ---------- */

  /**
   * The unique path of this request. Will be concatenated
   * with the unique ID of the microservice to form an
   * overall path hosted by the Vert.x server. For this request,
   * the path will be:
   * <code>/example-service/example-request</code>
   */
  public static final String REQUEST_PATH = "example-request";

  /* ---------- VARIABLES ---------- */

  /**
   * The message passed in by the user.
   */
  private String msg;

  /* ---------- CONSTRUCTOR ---------- */

  /**
   * All Request subclasses must have a blank constructor,
   * so that they can be created and parsed by
   * <code>Request.parse()</code> or so they can have
   * their requestPath retrieved by <code>HTTPIntake</code>.
   */
  public ExampleRequest() {
    super(REQUEST_PATH);
  }

  /* ---------- METHODS ---------- */

  /**
   * Retrieve the message passed in by the user.
   *
   * @return The message passed in by the user.
   */
  public String getMsg() {
    return msg;
  }

  /**
   * This function parses the parameters of the
   * request, given by the user, and attempts to
   * fill out all the details/variables of the
   * request from them. If there is an issue,
   * return false. Otherwise, return true.
   *
   * @param parameters the map of parameters
   * @return true if the parameters were parsed
   * successfully, false otherwise
   */
  @Override
  public boolean parse(Map<String, String> parameters) {

    // Pull the "msg" parameter
    msg = parameters.get("msg");

    // Success if the parameter exists
    return msg != null;

  }

}
