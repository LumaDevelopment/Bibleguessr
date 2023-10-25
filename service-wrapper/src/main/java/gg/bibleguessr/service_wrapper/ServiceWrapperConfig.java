package gg.bibleguessr.service_wrapper;

import gg.bibleguessr.backend_utils.RabbitMQConfiguration;

/**
 * The configuration object for the service wrapper.
 *
 * @param apiKey           The API key to validate with incoming HTTP requests.
 *                         A blank key means we don't check for an API key.
 * @param hostWithVertx    Whether to start up a web server to take requests.
 * @param vertxPort        The port to host the web server on.
 * @param hostWithRabbitMQ Whether to accept requests over RabbitMQ.
 * @param rabbitMQConfig   The configuration for RabbitMQ.
 */
public record ServiceWrapperConfig(
  String apiKey,
  boolean hostWithVertx,
  int vertxPort,
  boolean hostWithRabbitMQ,
  RabbitMQConfiguration rabbitMQConfig
) {

  /**
   * Gets the default configuration for the service wrapper:<br>
   * - {@code apiKey} is blank.<br>
   * - {@code hostWithVertx} is {@code true}<br>
   * - {@code vertxPort} is {@code 8888}<br>
   * - {@code hostWithRabbitMQ} is {@code false}<br>
   * - {@code rabbitMQConfig} is the default.<br>
   *
   * @return The default configuration for the service wrapper
   */
  @SuppressWarnings("unused")
  public static ServiceWrapperConfig getDefault() {
    return new ServiceWrapperConfig(
      "",
      true,
      8888,
      false,
      RabbitMQConfiguration.getDefault()
    );
  }

}
