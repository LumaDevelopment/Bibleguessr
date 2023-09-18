package gg.bibleguessr.service_wrapper;

/**
 * The configuration object for the service wrapper.
 */
public record ServiceWrapperConfig(
  boolean hostWithVertx,
  int vertxPort,
  boolean hostWithRabbitMQ,
  String rabbitMQUsername,
  String rabbitMQPassword,
  String rabbitMQHost,
  int rabbitMQPort,
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
   * - {@code rabbitMQUsername} is blank<br>
   * - {@code rabbitMQPassword} is blank<br>
   * - {@code rabbitMQHost} is blank<br>
   * - {@code rabbitMQPort} is {@code 5672}<br>
   * - {@code rabbitMQExchangeName} is {@code "exchange"}<br>
   * - {@code rabbitMQRequestsQueue} is {@code "requests"}<br>
   * - {@code rabbitMQResponsesQueue} is {@code "responses"}<br>
   * - {@code rabbitMQMicroserviceIDField} is {@code "microservice_id"}<br>
   * - {@code rabbitMQRequestPathField} is {@code "request_path"}<br>
   *
   * @return The default configuration for the service wrapper
   */
  public static ServiceWrapperConfig getDefault() {
    return new ServiceWrapperConfig(
      true,
      8888,
      false,
      "",
      "",
      "",
      5672,
      "exchange",
      "requests",
      "responses",
      "microservice_id",
      "request_path"
    );
  }

}
