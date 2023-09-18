package gg.bibleguessr.service_wrapper;

import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract class giving some of the functionality of a request
 * and defining functionality that the user must implement.
 * ALL SUBCLASSES MUST HAVE BLANK CONSTRUCTOR.
 */
public abstract class Request {

  /* ---------- VARIABLES ---------- */

  protected final String id;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Request that takes in an ID. Should be called by
   * subclass blank constructor.
   *
   * @param id the ID of this request
   */
  public Request(String id) {
    this.id = id;
  }

  /* ---------- METHODS ---------- */

  /**
   * Get the path of this Request type by concatenating
   * the microservice ID and this request ID.
   *
   * @param microservice the microservice this request falls under
   * @return the path of this Request type
   */
  public String getPath(Microservice microservice) {
    return "/" + microservice.getID() + "/" + id;
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
   * @param clazz the class of the Request subclass to parse into
   * @param parameters the parameters to parse
   * @return the new instance of the given Request subclass
   * @param <T> the Request subclass to parse into
   */
  public static <T extends Request> T parse(Class<T> clazz, Map<String, String> parameters) {

    if (clazz == null || parameters == null) {
      // Both parameters must be non-null.
      return null;
    }

    // Attempt to create new instance of clazz
    T request;
    try {
      request = clazz.newInstance();
    } catch (Exception e) {
      // Instantiation failure likely means mis-architectured Request.
      LoggerFactory
        .getLogger(clazz.getSimpleName())
        .error("Failed to instantiate Request, make sure all Request subclasses have a blank constructor!");
      throw new RuntimeException(e);
    }

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

}
