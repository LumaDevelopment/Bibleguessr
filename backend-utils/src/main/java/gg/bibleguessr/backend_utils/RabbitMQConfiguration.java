package gg.bibleguessr.backend_utils;

/**
 * A class which contains all configurable values that should be
 * considered when connecting to RabbitMQ.
 *
 * @param username            RabbitMQ server username.
 * @param password            RabbitMQ server password for username.
 * @param host                Host of the RabbitMQ server.
 * @param port                Port for the RabbitMQ server.
 * @param virtualHost         Virtual host of the RabbitMQ server to use.
 * @param exchangeName        Name of the exchange to bind to on the RabbitMQ server.
 * @param requestsQueue       Name of the queue where requests will be received.
 * @param responsesQueue      Name of the queue where responses will be sent.
 * @param microserviceIDField The field name for the microservice ID in the request JSON.
 * @param requestPathField    The field name for the request path in the request JSON.
 */
public record RabbitMQConfiguration(
        String username,
        String password,
        String host,
        int port,
        String virtualHost,
        String exchangeName,
        String requestsQueue,
        String responsesQueue,
        String microserviceIDField,
        String requestPathField
) {

    /**
     * Gets the default values of a Rabbit MQ configuration:<br>
     * - {@code username} is {@code "guest"}<br>
     * - {@code password} is {@code "guest"}<br>
     * - {@code host} is {@code "localhost"}<br>
     * - {@code port} is {@code 5672}<br>
     * - {@code virtualHost} is {@code ""}<br>
     * - {@code exchangeName} is {@code "exchange"}<br>
     * - {@code requestsQueue} is {@code "requests"}<br>
     * - {@code responsesQueue} is {@code "responses"}<br>
     * - {@code microserviceIDField} is {@code "microservice_id"}<br>
     * - {@code requestPathField} is {@code "request_path"}<br>
     *
     * @return The default value of a Rabbit MQ configuration.
     */
    public static RabbitMQConfiguration getDefault() {
        return new RabbitMQConfiguration(
                "guest",
                "guest",
                "localhost",
                5672,
                "",
                "exchange",
                "requests",
                "responses",
                "microservice_id",
                "request_path"
        );
    }

}
