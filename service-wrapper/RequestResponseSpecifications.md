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

## Self Service

**ID:** `service-wrapper`

### Get Running Microservice IDs

- **Request Path:** `get-ids`
- **Vert.x Path:** `/service-wrapper/get-ids`
- **Request Parameters:** None
- **Response Parameters:**
  - `ids` (Array) - An array of the IDS of all the services this Service Wrapper is running.

## Example Service

**ID:** `example-service`

### Example Request

- **Request Path:** `example-request`
- **Vert.x Path:** `/example-service/example-request`
- **Request Parameters:**
  - `msg` - Message of this request.
- **Response Parameters:**
  - `lengthDivisibleBy2` (Boolean) - Whether the length of the message of the request is evenly divisible by 2. Can
    be `true`
    or `false`.
