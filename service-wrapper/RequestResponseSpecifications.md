# Request/Response Specifications

## Notes

- Response parameters are always Strings.

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
