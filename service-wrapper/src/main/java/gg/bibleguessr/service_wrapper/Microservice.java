package gg.bibleguessr.service_wrapper;

import java.util.Collection;
import java.util.HashMap;

public abstract class Microservice {

  /* ---------- VARIABLES ---------- */

  /**
   * The ID of this microservice (concatenated
   * with Request IDs to form paths).
   */
  protected final String id;

  /**
   * A map from request path to Request types. Keeps
   * track of all Request types that belong to this
   * Microservice.
   */
  private final HashMap<String, Class<? extends Request>> requestTypes;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Initializes this Microservice object. Calls
   * initializeRequestTypesList().
   *
   * @param id The ID of the service.
   */
  public Microservice(String id) {

    if (id == null) {
      throw new RuntimeException("Microservice ID cannot be null!");
    }

    this.id = id;

    this.requestTypes = new HashMap<>();
    initializeRequestTypes();

  }

  /* ---------- METHODS ---------- */

  /**
   * Execute a request that is handled by this Microservice.
   * Request objects are passed into this Microservice by
   * the ServiceWrapper.
   *
   * @param request The request to execute.
   * @return The response to the request.
   */
  public abstract Response executeRequest(Request request);

  /**
   * Gets the ID of this microservice.
   *
   * @return The ID of this microservice.
   */
  public String getID() {
    return id;
  }

  /**
   * Gets the type of request that is associated
   * with the given request path.
   *
   * @param requestPath The path of the request.
   * @return The type of request associated with
   * the given path, or null if no such request
   * exists.
   */
  public Class<? extends Request> getRequestTypeFromPath(String requestPath) {

    if (requestPath == null) {
      return null;
    }

    return requestTypes.get(requestPath);

  }

  /**
   * Gets the types of requests that are associated
   * with this microservice.
   *
   * @return List of microservice request types.
   */
  public Collection<Class<? extends Request>> getRequestTypes() {
    return requestTypes.values();
  }

  /**
   * Adds this request type to the list of request types
   * of this microservice.
   *
   * @param requestType The request type to add.
   */
  public void initializeRequestType(Class<? extends Request> requestType) {
    Request request = Request.requestObjFromClass(requestType);
    requestTypes.put(request.getRequestPath(), requestType);
  }

  /**
   * Adds all request types associated with this
   * Microservice to the requestTypes list.
   */
  public abstract void initializeRequestTypes();

  /**
   * All operations that need to be done when the
   * Microservice shuts down.
   */
  public abstract void shutdown();

}
