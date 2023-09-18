# Request/Response Specifications

## Notes

- Response parameters are always Strings.
- RabbitMQ requires you to include the microservice ID and request path in your message JSON. This is so that the
  RabbitMQ consumer knows which microservice to send the response to. To see what JSON keys the consumer will look for,
  reference `ServiceWrapperConfig`'s `rabbitMQMicroserviceIDField` and `rabbitMQRequestPathField` fields.

## Error Codes

Vert.x will respond with the following error codes, depending on the situation:

- `400` - Request could not be parsed, malformed request.
- `404` - The requested path has not been registered.
- `500` - The server had some problem executing the request.

## Example Service

**ID:** `example-service`

### Example Request

- **Request Path:** `example-request`
- **Vert.x Path:** `/example-service/example-request`
- **Request Parameters:**
  - `uuid` (String) (Optional) - Unique request identifier.
  - `msg` (String) - Message of this request.
- **Response Parameters:**
  - `uuid` (Optional) - Matches UUID from request. Only useful for something like RabbitMQ where we can't
    deliver a direct response to a request.
  - `lengthDivisibleBy2` - Whether the length of the message of the request is evenly divisible by 2. Can be `true`
    or `false`.
