package gg.bibleguessr.service_wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import org.slf4j.LoggerFactory;

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
  private final ObjectNode content;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * Constructor for a Response. UUID can be null
   * if this response doesn't need to be uniquely
   * identified. However, this is passed
   * in/determined automatically by Request.getUUID()
   *
   * @param content The content of this Response, stored as JSON.
   * @param uuid    The unique identifier of this Response.
   */
  public Response(ObjectNode content, String uuid) {

    if (content == null) {
      throw new RuntimeException("Cannot make Response instance with null content!");
    }

    this.uuid = uuid;
    this.content = content;

  }

  /* ---------- METHODS ---------- */

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
   * Returns the content of this Response as a
   * JSON object. Will inject UUID into the content
   * if this Response has a UUID and that UUID
   * is not already in the content.
   *
   * @return A JSON object representing this Response instance.
   */
  public ObjectNode toJSONNode() {

    // Add "uuid" field if applicable.
    if (uuid != null && !uuid.isBlank() && !content.has("uuid")) {
      content.put("uuid", uuid);
    }

    return content;

  }

  /**
   * Serializes this Response instance as a JSON string.
   *
   * @return A JSON string representing this Response instance.
   */
  public String toJSONString() {

    try {

      return GlobalObjectMapper.get().writeValueAsString(toJSONNode());

    } catch (JsonProcessingException e) {

      // If this doesn't work, make a logger real quick and
      // notify the user that something went wrong.
      LoggerFactory.getLogger(Response.class.getSimpleName())
        .error("Failed to convert Response to JSON string!", e);
      return null;

    }

  }

}
