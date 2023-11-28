package gg.bibleguessr.backend_utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A globally accessible ObjectMapper instance, to
 * reduce the number of ObjectMapper instances, the
 * time spent creating them, and the memory used
 * maintaining them.
 */
public class GlobalObjectMapper {

    /**
     * The singular ObjectMapper instance. Shared
     * across the Bibleguessr backend.
     */
    private static ObjectMapper objectMapper;

    /**
     * This class is a utility class and shouldn't
     * be instantiated.
     */
    private GlobalObjectMapper() {
    }

    /**
     * Gets the global ObjectMapper instance.
     *
     * @return The ObjectMapper instance.
     */
    public static ObjectMapper get() {

        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        return objectMapper;

    }

    /**
     * Attempts to parse the given message as a JSON object.
     * If this fails at any point along the process, null is
     * returned.
     *
     * @param message The message to parse.
     * @return The parsed message as a JSON object, or null if
     * the message could not be parsed.
     */
    public static ObjectNode parseBytesAsJSONObject(byte[] message) {

        try {

            JsonNode jsonNode = get().readTree(message);
            if (jsonNode.isObject()) {
                return (ObjectNode) jsonNode;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Attempts to parse the given message as a JSON object.
     * If this fails at any point along the process, null is
     * returned.
     *
     * @param message The message to parse.
     * @return The parsed message as a JSON object, or null if
     * the message could not be parsed.
     */
    public static ObjectNode parseStringAsJSONObject(String message) {

        try {

            JsonNode jsonNode = get().readTree(message);
            if (jsonNode.isObject()) {
                return (ObjectNode) jsonNode;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }

    }

}
