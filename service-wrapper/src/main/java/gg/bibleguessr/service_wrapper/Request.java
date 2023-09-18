package gg.bibleguessr.service_wrapper;

import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract class giving some functionality of a request
 * and defining functionality that the user must implement.
 * ALL SUBCLASSES MUST HAVE BLANK CONSTRUCTOR.
 */
public abstract class Request {

  /* ---------- CONSTANTS ---------- */

  /**
   * The name of the parameter that contains the
   * Request UUID. Just a courtesy thing, can
   * be handled manually by Request implementer.
   */
  public static final String UUID_PARAMETER_NAME = "uuid";

  /* ---------- VARIABLES ---------- */

  /**
   * A sort of condensed version of the name,
   * used for constructing webserver paths.
   */
  protected final String requestPath;

  /**
   * The unique identifier of this Request instance.
   */
  protected String uuid;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Request that takes in the unique path of
   * the request. Should be called by
   * subclass blank constructor.
   *
   * @param requestPath the path of the request
   */
  public Request(String requestPath) {

    if (requestPath == null) {
      throw new RuntimeException("Request path cannot be null!");
    }

    this.requestPath = requestPath;
    this.uuid = "";

  }

  /* ---------- METHODS ---------- */

  /**
   * Get the path of the request, used
   * for the construction of webserver paths.
   *
   * @return the path of the request.
   */
  public String getRequestPath() {
    return requestPath;
  }

  /**
   * Gets the unique identifier of this request
   * object.
   *
   * @return The unique identifier of this request
   * object, or null if the Request object has no UUID.
   */
  public String getUUID() {

    if (!isIdentifiable()) {
      return null;
    }

    return uuid;

  }

  /**
   * Determines whether this Request is identifiable by
   * checking if it has a UUID set.
   *
   * @return Whether this request object is identifiable
   */
  public boolean isIdentifiable() {
    return this.uuid != null && !this.uuid.isBlank();
  }

  /**
   * Given the map of parameters, parse each parameter and
   * set it to the corresponding instance variable of
   * this request. If any parameter is invalid, or the
   * parsing goes wrong in any other way, return false.
   *
   * @param parameters the map of parameters
   * @return whether the parse was successful
   */
  public abstract boolean parse(Map<String, String> parameters);

  /**
   * Parses the given parameters into a new instance of the given
   * Request subclass. If the parse fails, returns null. If the
   * parse succeeds, returns the new instance.
   *
   * @param clazz      the class of the Request subclass to parse into
   * @param parameters the parameters to parse
   * @param <T>        the Request subclass to parse into
   * @return the new instance of the given Request subclass
   */
  public static <T extends Request> T parse(Class<T> clazz, Map<String, String> parameters) {

    if (clazz == null || parameters == null) {
      // Both parameters must be non-null.
      return null;
    }

    // Attempt to create new instance of clazz
    T request;
    try {
      request = clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      // Instantiation failure likely means mis-architectured Request.
      LoggerFactory
        .getLogger(clazz.getSimpleName())
        .error("Failed to instantiate Request, make sure all Request subclasses have a blank constructor!");
      throw new RuntimeException(e);
    }

    // Attempt to set the UUID of the request.
    // If the parameters map doesn't have a
    // UUID, then the setUUID() function will
    // handle that automatically
    request.setUUID(parameters.remove(UUID_PARAMETER_NAME));

    // Attempt to parse request
    boolean success = request.parse(parameters);

    // If the parse worked, great,
    // return the object. If not,
    // return null.
    if (success) {
      return request;
    } else {
      return null;
    }

  }

  /**
   * Sets the unique identifier of this
   * Request object.
   *
   * @param uuid The new unique identifier
   */
  public void setUUID(String uuid) {

    if (uuid == null) {
      return;
    }

    this.uuid = uuid;

  }

}
