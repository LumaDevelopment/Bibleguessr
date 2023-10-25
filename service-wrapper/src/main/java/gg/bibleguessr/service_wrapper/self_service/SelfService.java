package gg.bibleguessr.service_wrapper.self_service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import gg.bibleguessr.service_wrapper.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The service that gives information about the Service
 * Wrapper itself to the API gateway.
 */
public class SelfService extends Microservice {

  /* ---------- CONSTANTS ---------- */

  /**
   * The name of the logger for this class.
   */
  public static final String LOGGER_NAME = SelfService.class.getSimpleName();

  /**
   * The ID of this microservice.
   */
  public static final String ID = "service-wrapper";

  /* ---------- INSTANCE VARIABLES ---------- */

  /**
   * The logger for this class.
   */
  private final Logger logger;

  /**
   * The Service Wrapper instance, used to
   * retrieve all currently running microservices,
   * so they can be included in the Get IDs
   * Request response.
   */
  private final ServiceWrapper wrapper;

  /* ---------- CONSTRUCTOR ---------- */

  /**
   * Main constructor.
   *
   * @param wrapper The Service Wrapper instance, used
   *                to retrieve all currently running
   *                microservices, so they can be
   *                included in the Get IDs Request
   *                response.
   */
  public SelfService(ServiceWrapper wrapper) {
    super(ID);
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.wrapper = wrapper;
  }

  /* ---------- METHODS ---------- */

  private Response executeGetIDsRequest(GetIDsRequest request) {

    ObjectNode content = GlobalObjectMapper.get().createObjectNode();
    ArrayNode ids = GlobalObjectMapper.get().createArrayNode();

    // Collect ids array
    for (Microservice service : wrapper.getRunningMicroservices()) {

      if (service.getID().equals(SelfService.ID)) {
        // Don't include self-service in the list,
        // because if the API gateway receives a
        // service wrapper-specific request, it
        // shouldn't be able to fulfill it, because
        // it manages multiple service wrappers
        continue;
      }

      ids.add(service.getID());
    }

    // Add to content JSON object
    content.set("ids", ids);

    return new Response(content, request.getUUID());

  }

  @Override
  public Response executeRequest(Request request) {

    if (request instanceof GetIDsRequest getIDsRequest) {
      return executeGetIDsRequest(getIDsRequest);
    } else {
      // Unknown type of request
      logger.error("Received Request of unknown type: {}", request.getClass().getSimpleName());
      return null;
    }

  }

  @Override
  public void initializeRequestTypesMap() {
    initializeRequestType(GetIDsRequest.class);
  }

  @Override
  public void shutdown() {
  }

}
