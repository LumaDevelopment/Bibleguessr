package gg.bibleguessr.api_gateway;

import gg.bibleguessr.api_gateway.comms.CommsOrchestrator;
import gg.bibleguessr.backend_utils.RabbitMQConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ServiceWrapperDetector {

    /**
     * Given a list of hosts to running Service Wrappers,
     * return a map of service IDs to all hosts that host
     * a service with that ID.
     *
     * @param serviceWrapperHosts The hosts of the service wrappers
     *                            we want to connect to/use.
     * @param orchestrator        The CommsOrchestrator, through
     *                            which we can send requests.
     * @return A map of service IDs to all hosts that host
     * a service with that ID.
     */
    public static Map<String, HashSet<String>> detectHTTPServiceWrappers(String[] serviceWrapperHosts, CommsOrchestrator orchestrator) {
        return new HashMap<>();
    }

    /**
     * Given a RabbitMQ configuration, detect all
     * running Service Wrappers connected to the
     * RabbitMQ broker, and the services they run.
     *
     * @param rabbitMQConfig The RabbitMQ configuration.
     * @param orchestrator   The CommsOrchestrator, through
     *                       which we can send requests.
     * @return A set of the IDs of all services that are
     * hosted across all service wrappers connected to
     * the RabbitMQ broker.
     */
    public static HashSet<String> detectRabbitMQServiceWrappers(RabbitMQConfiguration rabbitMQConfig, CommsOrchestrator orchestrator) {
        return new HashSet<>();
    }

}
