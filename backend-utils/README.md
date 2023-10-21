# Backend Utils

There ends up being a good bit of common functionality required by different Bibleguessr services. Particularly, the
service wrapper and API gateway have a lot of overlap. So, this library contains some classes that help out the rest of
our microservices.

## Classes

- `BibleguessrUtilities` - Generic utility class. Helps with reading a configuration file into a configuration object
  and getting the current memory in use.
- `CommsCallback` - A class used to indicate and notify if a communication (HTTP, RabbitMQ, etc.) was a success or fa
  failure. Additionally, provides information like error code if the communication failed, or content if the
  communication succeeded.
- `GlobalObjectMapper` - Stores a global ObjectMapper instance to be shared across all applicable backend applications.
  This is done because ObjectMapper instances take time to make, use a lot of memory, and are thread-safe. So, it makes
  sense to have one instance available to all. Additionally, this class has a method which parses a JSON string into a
  Java object (`ObjectNode`).
- `RabbitMQConfiguration` - A record with all values needed to define the location, credentials, and functionality of a
  RabbitMQ server in a Bibleguessr application.
- `StatusCode` - An enum representing all the status codes that a Bibleguessr application can return. This is used
  instead of raw integers to make it easier to understand what a status code means.