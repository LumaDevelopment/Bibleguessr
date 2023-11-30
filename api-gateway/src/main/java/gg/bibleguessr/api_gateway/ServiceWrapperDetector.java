package gg.bibleguessr.api_gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.api_gateway.comms.CommsOrchestrator;
import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.backend_utils.RabbitMQConfiguration;
import gg.bibleguessr.backend_utils.StatusCode;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.self_service.GetIDsRequest;
import gg.bibleguessr.service_wrapper.self_service.SelfService;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ServiceWrapperDetector {

    /**
     * Given a list of sockets of running Service Wrappers,
     * return a map of service IDs to all sockets that point
     * to a service with that ID.
     *
     * @param serviceWrapperSockets The sockets of the service wrappers
     *                              we want to connect to/use.
     * @param orchestrator          The CommsOrchestrator, through
     *                              which we can send requests.
     * @return A map of service IDs to all sockets that point
     * to a service with that ID.
     */
    public static Map<String, HashSet<String>> detectHTTPServiceWrappers(List<String> serviceWrapperSockets, CommsOrchestrator orchestrator) {

        Logger logger = LoggerFactory.getLogger(ServiceWrapperDetector.class.getSimpleName());

        // Concurrency variables
        Map<String, HashSet<String>> serviceIDToSockets = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(serviceWrapperSockets.size());

        // Loop through all sockets and send a request to each
        for (String socket : serviceWrapperSockets) {

            // Start each request in a new thread
            new Thread(() -> {

                CommsCallback callback = new CommsCallback() {

                    @Override
                    public void onSuccess(String content) {

                        ObjectNode obj = GlobalObjectMapper.parseStringAsJSONObject(content);

                        if (obj == null) {
                            logger.error("Received non-JSON response from service wrapper: {}", content);
                            finish();
                            return;
                        }

                        // Ensure ids array is intact
                        JsonNode ids = obj.get("ids");

                        if (ids == null || !ids.isArray()) {
                            logger.error("Service Wrapper's get-data response did not contain an array of IDs!");
                            finish();
                            return;
                        }

                        // Iterate through all the elements of the ids array
                        for (int i = 0; i < ids.size(); i++) {

                            // Get what should be an ID
                            JsonNode rawID = ids.get(i);

                            // Insure it's actually an ID
                            if (!rawID.isTextual()) {
                                logger.error("Service Wrapper's get-data response contained a non-textual ID!");
                                continue;
                            }

                            // Add to map
                            String id = rawID.asText();

                            synchronized (serviceIDToSockets) {
                                if (!serviceIDToSockets.containsKey(id)) {
                                    serviceIDToSockets.put(id, new HashSet<>());
                                }

                                serviceIDToSockets.get(id).add(socket);
                            }

                        }

                        finish();

                    }

                    @Override
                    public void onFailure(StatusCode errorCode) {
                        logger.trace("Failed to get IDs from service wrapper with socket \"" + socket + "\". " +
                                "Error code: {}", errorCode);
                        finish();
                    }

                    private void finish() {
                        latch.countDown();
                    }

                };

                // Get a host from the socket
                String[] socketComponents = socket.split(":");
                String host = socketComponents[0];

                // Build a URL
                HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                        .scheme("http")
                        .host(host)
                        .addPathSegment(SelfService.ID)
                        .addPathSegment(GetIDsRequest.REQUEST_PATH);

                // If the socket has a port, add it to the URL
                if (socketComponents.length > 1) {
                    urlBuilder.port(Integer.parseInt(socketComponents[1]));
                }

                // Send the request to the orchestrator
                orchestrator.makeHTTPRequest(urlBuilder, callback);

            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for response from service wrappers over HTTP!", e);
        }

        logger.debug("Detected {} service wrappers using HTTP!", serviceIDToSockets.size());

        return serviceIDToSockets;

    }

    /**
     * Given the communications orchestrator,
     * send a RabbitMQ request which will prompt
     * all service wrappers to respond with which
     * services they have running. From here, compile
     * a set of services that we can access. Uses the
     * RabbitMQ configuration, so we have the microservice
     * ID field and request path field names.
     *
     * @param config       The RabbitMQ configuration.
     * @param orchestrator The CommsOrchestrator, through
     *                     which we can send requests.
     * @return A set of the IDs of all services that are
     * hosted across all service wrappers connected to
     * the RabbitMQ broker.
     */
    public static HashSet<String> detectRabbitMQServiceWrappers(RabbitMQConfiguration config, CommsOrchestrator orchestrator) {

        Logger logger = LoggerFactory.getLogger(ServiceWrapperDetector.class.getSimpleName());
        HashSet<String> serviceIDs = new HashSet<>();

        // Generate a unique ID for this request
        String uuid = UUID.randomUUID().toString();

        // Create JSON object
        ObjectNode request = GlobalObjectMapper.get().createObjectNode();

        // Put in meta-level parameters like UUID,
        // microservice ID and request path.
        request.put(Request.UUID_PARAMETER_NAME, uuid);
        request.put(config.microserviceIDField(), SelfService.ID);
        request.put(config.requestPathField(), GetIDsRequest.REQUEST_PATH);

        byte[] body;

        try {
            body = GlobalObjectMapper.get().writeValueAsBytes(request);
        } catch (JsonProcessingException e) {
            logger.error("Encountered error while converting request to byte array!", e);
            return serviceIDs;
        }

        // Make the request and get the responses.
        List<ObjectNode> responses = orchestrator.makeRabbitMQRequest(uuid, body);

        // Iterate through all responses and add IDs to set
        while (!responses.isEmpty()) {
            ObjectNode response = responses.remove(0);
            JsonNode idsProperty = response.get("ids");

            if (idsProperty == null || !idsProperty.isArray()) {
                logger.warn("Received response from service wrapper that did not contain an array of IDs!");
                continue;
            }

            // Iterate through all the elements of the ids array
            for (int i = 0; i < idsProperty.size(); i++) {

                // Get what should be an ID
                JsonNode rawID = idsProperty.get(i);

                // Insure it's actually an ID
                if (!rawID.isTextual()) {
                    logger.warn("Service Wrapper's get-data response contained a non-textual ID!");
                    continue;
                }

                // Add to set
                String id = rawID.asText();
                serviceIDs.add(id);

            }

        }

        logger.debug("Detected {} service wrappers using RabbitMQ!", serviceIDs.size());

        // Finally, add all service IDs
        return serviceIDs;

    }

}
