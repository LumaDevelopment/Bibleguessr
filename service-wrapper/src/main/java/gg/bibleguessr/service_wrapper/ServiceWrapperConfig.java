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
  int vertxPort
) {

  /**
   * Gets the default configuration for the service wrapper, where
   * {@code hostWithVertx} is {@code true}, {@code hostWithRabbitMQ}
   * is {@code false}, and {@code vertxPort} is {@code 8888}.
   *
   * @return The default configuration for the service wrapper
   */
  public static ServiceWrapperConfig getDefault() {
    return new ServiceWrapperConfig(
      true,
      false,
      8888
    );
  }

}
