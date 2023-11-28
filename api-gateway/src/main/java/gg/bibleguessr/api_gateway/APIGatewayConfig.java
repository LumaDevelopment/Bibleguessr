package gg.bibleguessr.api_gateway;

import gg.bibleguessr.api_gateway.comms.CommsProtocol;
import gg.bibleguessr.backend_utils.RabbitMQConfiguration;

import java.util.List;

/**
 * @param port                         The port on which the API Gateway takes HTTP requests.
 * @param reqExecutionProtocol         The communication protocol used to connect to the
 *                                     configured Service Wrappers. We execute requests by
 *                                     sending messages over this protocol to the Service
 *                                     Wrappers. Possible options:<br>
 *                                     - <code>HTTP</code><br>
 *                                     - <code>RabbitMQ</code>
 * @param wrapperDetectionIntervalInMs The interval in milliseconds at which the API Gateway
 *                                     will detect service wrappers and check which ones are
 *                                     still alive.
 * @param apiKey                       The API key used to identify ourselves to service wrappers
 *                                     we send HTTP requests to. If this value is blank, it will
 *                                     not be sent.
 * @param httpSockets                  The sockets of all Service Wrappers we are connected to
 *                                     using HTTP. Only used if <code>reqExecutionProtocol</code>
 *                                     is <code>HTTP</code>.
 * @param allowedCorsOrigins           The origins that are allowed to make CORS requests to the
 *                                     API Gateway. Only used if <code>reqExecutionProtocol</code>
 *                                     is <code>HTTP</code>.
 * @param rabbitMQConfig               The configuration for RabbitMQ. Requests are made to all possible
 *                                     Service Wrappers connected to this broker. Only used if
 *                                     <code>reqExecutionProtocol</code> is <code>RabbitMQ</code>.
 * @param singleResponseTimeoutInMs    The timeout in milliseconds for single-response requests. How long
 *                                     to wait for a response to a request before declaring that the
 *                                     communication failed.
 * @param multiResponseTimeoutInMs     The timeout in milliseconds for multi-response requests. How long
 *                                     to go without receiving a response before declaring that all
 *                                     responses have been received.
 */
public record APIGatewayConfig(
        int port,
        CommsProtocol reqExecutionProtocol,
        long wrapperDetectionIntervalInMs,
        String apiKey,
        List<String> httpSockets,
        List<String> allowedCorsOrigins,
        RabbitMQConfiguration rabbitMQConfig,
        long singleResponseTimeoutInMs,
        long multiResponseTimeoutInMs
) {

    /**
     * Gets the default configuration for the API Gateway:<br>
     * - {@code port} is {@code 8891}<br>
     * - {@code reqExecutionProtocol} is {@code HTTP}<br>
     * - {@code wrapperDetectionIntervalInMs} is {@code 5_000} (5 seconds).<br>
     * - {@code apiKey} is blank.<br>
     * - {@code httpSockets} is {@code ["localhost:8890"]}<br>
     * - {@code allowedCorsOrigins} is {@code ["http://localhost:5173"]}<br>
     * - {@code rabbitMQConfig} is the default.<br>
     * - {@code singleResponseTimeoutInMs} is {@code 500} (500 milliseconds).<br>
     * - {@code multiResponseTimeoutInMs} is {@code 300} (300 milliseconds).
     *
     * @return The default configuration for the API Gateway
     */
    public static APIGatewayConfig getDefault() {
        return new APIGatewayConfig(
                8891,
                CommsProtocol.HTTP,
                15_000,
                "",
                List.of("localhost:8890"),
                List.of("http://localhost:5173"),
                RabbitMQConfiguration.getDefault(),
                500,
                300
        );
    }

}
