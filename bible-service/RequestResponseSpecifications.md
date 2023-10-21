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
    - `bibleVersion` - The version of the Bible the text is in.
    - `bookName` - The name of the book the random verse is pulled from according, to the chosen version.
    - `chapter` - The number of the chapter the verse is from.
    - `verseNumber` - The number of the verse within its chapter.
    - `verseArray` - The array of verse text, including any context verses that were requested.
    - `localVerseIndex` - The index of the random verse within the `verseArray`. This is useful for picking out the
      random verse from the context verses. The UI can use this to highlight the verse with the actual reference that
      needs to be guessed.
    - `globalVerseIndex` - The index of the random verse within the entire Bible.