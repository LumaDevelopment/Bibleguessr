# Request/Response Specifications

## Notes

- Request parameters are always Strings.
- Response parameters have JSON types, as response is always formatted as JSON.
- RabbitMQ requires you to include the microservice ID and request path in your message JSON. This is so that the
  RabbitMQ consumer knows which microservice to send the response to. To see what JSON keys the consumer will look for,
  reference `ServiceWrapperConfig`'s `rabbitMQMicroserviceIDField` and `rabbitMQRequestPathField` fields.

## Error Codes

Vert.x will respond with the following error codes, depending on the situation:

- `400` - Request could not be parsed, malformed request.
- `403` - API key missing or incorrect (HTTP only).
- `404` - The requested path has not been registered.
- `406` - URL formatted incorrectly (HTTP only).
- `415` - The API Gateway or Service Wrapper does not have the microservice registered.
- `500` - The server had some problem executing the request.

## Common Requests/Response Parameters

All requests and responses have the following parameters in common:

- **Request Parameters:**
    - `apiKey` (Required with HTTP) - Used to authenticate requests coming in on the Service Wrapper's web server.
    - `uuid` (Optional) - Unique request identifier.
- **Response Parameters:**
    - `uuid` (Text) (Optional) - Matches UUID from request. Only useful for something like RabbitMQ where we can't
      deliver a direct response to a request.
    - `error` (Integer) (Potential, RabbitMQ only) - If an error was encountered while executing the request, a response
      will be sent with the `error` attribute and an integer error code, which represents a `StatusCode` object/HTTP
      status code.

## Guess Counter Service

**ID:** `guess-counter`

### Get Count Request

- **Request Path:** `get-count`
- **Vert.x Path:** `/guess-counter/get-count`
- **Request Parameters:** None
- **Response Parameters:**
    - `count` (Long) - The number of guesses that have been made.

### Increment Count Request

- **Request Path:** `increment-count`
- **Vert.x Path:** `/guess-counter/increment-count`
- **Request Parameters:** None
- **Response Parameters:** None
