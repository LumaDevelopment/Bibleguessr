package gg.bibleguessr.backend_utils;

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
     * There is no request with the given microservice
     * ID and request path that is recognized by this
     * microservice/API gateway.
     */
    INVALID_PATH(404),

    /**
     * There was some issue within the microservice
     * or API gateway that prevented the request from
     * being processed.
     */
    INTERNAL_ERROR(500);

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
     * Get the actual HTTP status code.
     *
     * @return The HTTP status code.
     */
    public int getStatusCode() {
        return this.httpStatusCode;
    }

}
