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
- `404` - The requested path has not been registered.
- `500` - The server had some problem executing the request.

## Bible Service

**ID:** `bible`

### Frontend Bible Data Request

- **Request Path:** `get-bible-data`
- **Vert.x Path:** `/bible/get-bible-data`
- **Request Parameters:**
    - `uuid` (Optional) - Unique request identifier.
- **Response Parameters:**
    - `uuid` (Text) (Optional) - Matches UUID from request. Only useful for something like RabbitMQ where we can't
      deliver a direct response to a request.
    - `bibleNames` (Array) - The name of all versions this service has available.
        - This array is composed entirely of Text.
    - `bibleBookNames` (Object) - A dictionary from version (Text) to an Array of Text, representing the name of every
      book according to that version.
        - This object has Text keys, and its values are Arrays composed entirely of Text.
    - `dataMatrix` (Array) - An array, where each element represents one book of the bible.
        - Each element is an array, where each element represents a chapter in that book. The value of that element
          (Number/Integer) is the number of verses in that chapter.

### Random Verse Request

- **Request Path:** `random-verse`
- **Vert.x Path:** `/bible/random-verse`
- **Request Parameters:**
    - `uuid` (Optional) - Unique request identifier.
    - `version` - The name of the Bible version to pull the text from.
    - `numOfContextVerses` - An integer. The number of verses to include before and after the random verse to provide
      context. The minimum of this number is `0`, the maximum is `15550`.
- **Response Parameters:**
    - `uuid` (Text) (Optional) - Matches UUID from request. Only useful for something like RabbitMQ where we can't
      deliver a direct response to a request.
    - `error` (Integer) (On Failure) - An error code if an issue arises while executing this request.
        - `0` - Invalid version. Either the version is blank or this service does not have the given version.
        - `1` - Invalid number of context verses. Either the number of context verses is less than the minimum or more
          than the maximum.
