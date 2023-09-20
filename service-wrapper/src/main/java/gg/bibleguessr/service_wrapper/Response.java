package gg.bibleguessr.service_wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Class that represents the response
 * to a Request. Can be serialized as
 * JSON.
 */
public class Response {

  /* ---------- VARIABLES ---------- */

  /**
   * Unique identifier for this response. Doesn't
   * necessarily need to be set.
   */
  private final String uuid;

  /**
   * The content of this response. Necessary for
   * the Response to function.
   */
  private final Map<String, String> content;

  /**
   * An ObjectMapper instance variable for serializing
   * this Response as JSON. Not set by default because
   * there may be some instances where a Response
   * never needs to be serialized, like in a bundled
   * deployment. Instead, it is accessed by getMapper()
   * and initialized at that point if it hasn't been
   * already.
   */
  private ObjectMapper mapper;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Constructor for a Response. UUID can be null
   * if this response doesn't need to be uniquely
   * identified. However, this is passed
   * in/determined automatically by Request.getUUID()
   *
   * @param content The content of this Response.
   * @param uuid    The unique identifier of this Response.
   */
  public Response(Map<String, String> content, String uuid) {

    if (content == null) {
      throw new RuntimeException("Cannot make Response instance with null content!");
    }

    this.uuid = uuid;
    this.content = content;

  }

  /* ---------- METHODS ---------- */

  /**
   * Retrieves the content of this response,
   * which is a key-value map.
   *
   * @return The content of this response.
   */
  public Map<String, String> getContent() {
    return content;
  }

  /**
   * Lazy loading ObjectMapper creation
   * and retrieval.
   *
   * @return An ObjectMapper instance.
   */
  private ObjectMapper getMapper() {

    if (mapper == null) {
      mapper = new ObjectMapper();
    }

    return mapper;

  }

  /**
   * Retrieves the unique identifier of
   * this Response, which should match the
   * unique identifier of the Request which
   * prompted it.
   *
   * @return The unique identifier of this Response.
   */
  public String getUUID() {
    return uuid;
  }

  /**
   * Converts this Response instance to a JSON object.
   *
   * @return A JSON object representing this Response instance.
   */
  public ObjectNode toJSONNode() {

    ObjectNode rootNode = getMapper().createObjectNode();

    // Add "uuid" field if applicable.
    if (uuid != null && !uuid.isBlank()) {
      rootNode.put("uuid", uuid);
    }

    // Add key-value pairs from the content map
    content.forEach(rootNode::put);

    return rootNode;

  }

  /**
   * Serializes this Response instance as a JSON string.
   *
   * @return A JSON string representing this Response instance.
   */
  public String toJSONString() {

    try {

      return getMapper().writeValueAsString(toJSONNode());

    } catch (JsonProcessingException e) {

      // If this doesn't work, make a logger real quick and
      // notify the user that something went wrong.
      LoggerFactory.getLogger(Response.class.getSimpleName())
        .error("Failed to convert Response to JSON string!", e);
      return null;

    }

  }

}
