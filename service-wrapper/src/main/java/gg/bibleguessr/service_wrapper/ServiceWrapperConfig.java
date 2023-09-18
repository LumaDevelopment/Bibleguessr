package gg.bibleguessr.service_wrapper;

/**
 * The configuration object for the service wrapper.
 *
 * @param hostWithVertx    Whether to host services with Vert.x
 * @param hostWithRabbitMQ Whether to enable inter-service communication with RabbitMQ
 * @param vertxPort        The port to host Vert.x services on, if applicable
 */
public record ServiceWrapperConfig(
  boolean hostWithVertx,
  boolean hostWithRabbitMQ,
  int vertxPort,
  String rabbitMQUsername,
  String rabbitMQPassword,
  String rabbitMQHost,
  int rabbitMQPort
) {

  /**
   * Gets the default configuration for the service wrapper:<br>
   * - {@code hostWithVertx} is {@code true}<br>
   * - {@code hostWithRabbitMQ} is {@code false}<br>
   * - {@code vertxPort} is {@code 8888}<br>
   * - {@code rabbitMQUsername} is blank<br>
   * - {@code rabbitMQPassword} is blank<br>
   * - {@code rabbitMQHost} is blank<br>
   * - {@code rabbitMQPort} is {@code 5672}<br>
   *
   * @return The default configuration for the service wrapper
   */
  public static ServiceWrapperConfig getDefault() {
    return new ServiceWrapperConfig(
      true,
      false,
      8888,
      "",
      "",
      "",
      5672
    );
  }

}
