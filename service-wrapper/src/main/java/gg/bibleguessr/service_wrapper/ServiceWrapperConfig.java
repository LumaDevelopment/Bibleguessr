package gg.bibleguessr.service_wrapper;

/**
 * The configuration object for the service wrapper.
 *
 * @param hostWithVertx               Whether to start up a web server to take requests.
 * @param vertxPort                   The port to host the web server on.
 * @param hostWithRabbitMQ            Whether to accept requests over RabbitMQ.
 * @param rabbitMQUsername            Username for the RabbitMQ server.
 * @param rabbitMQPassword            Password for the RabbitMQ server.
 * @param rabbitMQHost                Host for the RabbitMQ server.
 * @param rabbitMQPort                Port for the RabbitMQ server.
 * @param rabbitMQVirtualHost         Virtual host of the RabbitMQ server to use.
 * @param rabbitMQExchangeName        Name of the exchange to bind to on the RabbitMQ server.
 * @param rabbitMQRequestsQueue       Name of the queue where requests will be received.
 * @param rabbitMQResponsesQueue      Name of the queue where responses will be sent.
 * @param rabbitMQMicroserviceIDField The field name for the microservice ID in the request JSON.
 * @param rabbitMQRequestPathField    The field name for the request path in the request JSON.
 */
public record ServiceWrapperConfig(
  boolean hostWithVertx,
  int vertxPort,
  boolean hostWithRabbitMQ,
  String rabbitMQUsername,
  String rabbitMQPassword,
  String rabbitMQHost,
  int rabbitMQPort,
  String rabbitMQVirtualHost,
  String rabbitMQExchangeName,
  String rabbitMQRequestsQueue,
  String rabbitMQResponsesQueue,
  String rabbitMQMicroserviceIDField,
  String rabbitMQRequestPathField
) {

  /**
   * Gets the default configuration for the service wrapper:<br>
   * - {@code hostWithVertx} is {@code true}<br>
   * - {@code vertxPort} is {@code 8888}<br>
   * - {@code hostWithRabbitMQ} is {@code false}<br>
   * - {@code rabbitMQUsername} is {@code "guest"}<br>
   * - {@code rabbitMQPassword} is {@code "guest"}<br>
   * - {@code rabbitMQHost} is {@code "localhost"}<br>
   * - {@code rabbitMQPort} is {@code 5672}<br>
   * - {@code rabbitMQVirtualHost} is {@code ""}<br>
   * - {@code rabbitMQExchangeName} is {@code "exchange"}<br>
   * - {@code rabbitMQRequestsQueue} is {@code "requests"}<br>
   * - {@code rabbitMQResponsesQueue} is {@code "responses"}<br>
   * - {@code rabbitMQMicroserviceIDField} is {@code "microservice_id"}<br>
   * - {@code rabbitMQRequestPathField} is {@code "request_path"}<br>
   *
   * @return The default configuration for the service wrapper
   */
  @SuppressWarnings("unused")
  public static ServiceWrapperConfig getDefault() {
    return new ServiceWrapperConfig(
      true,
      8888,
      false,
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
