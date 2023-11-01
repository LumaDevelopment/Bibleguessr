package gg.bibleguessr.backend_utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents status codes that are returned by the
 * service wrapper and the API gateway.
 */
public enum StatusCode {

    /**
     * No issues encountered.
     */
    OK(200),

    /**
     * A valid request was made, but, it had an invalid
     * number of parameters, invalid types of parameters,
     * or invalid values for parameters.
     */
    MALFORMED_REQUEST(400),

    /**
     * The API key is missing or invalid.
     */
    BAD_API_KEY(403),

    /**
     * There is no request with the given path that is
     * recognized by the corresponding microservice
     * running on this service wrapper/API gateway.
     */
    INVALID_PATH(404),

    /**
     * A bad URL passed into a service wrapper/API gateway
     * web server.
     */
    HTTP_BAD_URL(406),

    /**
     * No microservice with the given ID is running
     * on or recognized by this service wrapper/API
     * gateway.
     */
    NO_MICROSERVICE_WITH_ID(415),

    /**
     * There was some issue within the microservice
     * or API gateway that prevented the request from
     * being processed.
     */
    INTERNAL_ERROR(500);

    // Mapping from integer to object
    private static final Map<Integer, StatusCode> intToObjMap = new HashMap<>();
    static {
        for (StatusCode statusCode : StatusCode.values()) {
            intToObjMap.put(statusCode.getStatusCode(), statusCode);
        }
    }

    /**
     * The actual HTTP status code value.
     */
    private final int httpStatusCode;

    /**
     * Used in the instantiation of StatusCode enums.
     *
     * @param httpStatusCode The actual HTTP status code value.
     */
    StatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Get the StatusCode object corresponding with the given
     * status code.
     *
     * @param i The status code.
     * @return The corresponding StatusCode object, or null
     * if none exists for the given code.
     */
    public static StatusCode fromInt(int i) {
        return intToObjMap.get(i);
    }

    /**
     * Get the actual HTTP status code.
     *
     * @return The HTTP status code.
     */
    public int getStatusCode() {
        return this.httpStatusCode;
    }

}
