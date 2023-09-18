package gg.bibleguessr.service_wrapper;

import java.util.LinkedList;
import java.util.List;

public abstract class Microservice {

  /* ---------- VARIABLES ---------- */

  /**
   * The ID of this microservice (concatenated
   * with Request IDs to form paths).
   */
  protected final String id;

  /**
   * LinkedList that keeps track of all Request types
   * that belong to this Microservice.
   */
  protected final List<Class<? extends Request>> requestTypes;

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

    this.requestTypes = new LinkedList<>();
    initializeRequestTypesList();

  }

  /* ---------- METHODS ---------- */

  /**
   * Gets the ID of this microservice.
   *
   * @return The ID of this microservice.
   */
  public String getID() {
    return id;
  }

  /**
   * Gets the types of requests that are associated
   * with this microservice.
   *
   * @return List of microservice request types.
   */
  public List<Class<? extends Request>> getRequestTypes() {
    return requestTypes;
  }

  /**
   * Adds all request types associated with this
   * Microservice to the requestTypes list.
   */
  public abstract void initializeRequestTypesList();

  /**
   * All operations that need to be done when the
   * Microservice shuts down.
   */
  public abstract void shutdown();

}
