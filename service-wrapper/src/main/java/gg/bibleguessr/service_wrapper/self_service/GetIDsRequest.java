package gg.bibleguessr.service_wrapper.self_service;

import gg.bibleguessr.service_wrapper.Request;

import java.util.Map;

/**
 * Request which allows an API gateway or any other
 * interested party to retrieve the IDs of all
 * microservices running on this Service Wrapper.
 * Does not include the ID of the
 * <code>SelfService</code>.
 */
public class GetIDsRequest extends Request {

  /* ---------- CONSTANTS ---------- */

  /**
   * The unique path of this request. Will be concatenated
   * with the unique ID of the microservice to form an
   * overall path hosted by the Vert.x server. For this request,
   * the path will be:
   * <code>/service-wrapper/get-ids</code>
   */
  public static final String REQUEST_PATH = "get-ids";

  /* ---------- CONSTRUCTOR ---------- */

  /**
   * All Request subclasses must have a blank constructor,
   * so that they can be created and parsed by
   * <code>Request.parse()</code> or so they can have
   * their requestPath retrieved by <code>IntakeMgr</code>.
   */
  public GetIDsRequest() {
    super(REQUEST_PATH);
  }

  /* ---------- METHODS ---------- */

  /***
   * There are no parameters to parse with this
   * request, so, always returns true, and
   * parameters map is not used (unless it
   * has a UUID).
   *
   * @param parameters the map of parameters
   * @return true
   */
  @Override
  public boolean parse(Map<String, String> parameters) {
    return true;
  }

}
