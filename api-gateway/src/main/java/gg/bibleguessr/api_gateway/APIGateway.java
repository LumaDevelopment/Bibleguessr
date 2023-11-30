package gg.bibleguessr.api_gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.api_gateway.comms.CommsOrchestrator;
import gg.bibleguessr.api_gateway.comms.CommsProtocol;
import gg.bibleguessr.backend_utils.BibleguessrUtilities;
import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.backend_utils.StatusCode;
import gg.bibleguessr.service_wrapper.Request;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class APIGateway {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of this class's logger, which
     * is the name of this class.
     */
    public static final String LOGGER_NAME = APIGateway.class.getSimpleName();

    /**
     * Where the API Gateway config file is located if no
     * other path is specified.
     */
    public static final String DEFAULT_CONFIG_FILE_PATH = "api_gateway_config.json";

    /* ---------- INSTANCE VARIABLES ---------- */

    // Common Variables

    /**
     * The logger for this class.
     */
    private final Logger logger;

    /**
     * The location of the config file.
     */
    private final File configFile;

    /**
     * The actual configuration object for the
     * API Gateway, read and written from and
     * to the config file via jackson-databind.
     */
    private APIGatewayConfig config;

    /**
     * Used for scheduling service wrapper detection.
     */
    private final Timer timer;

    // Managers

    /**
     * The communications orchestrator that manages
     * all the communications between the frontend
     * and the API Gateway, as well as between the
     * API Gateway and all the service wrappers.
     */
    private CommsOrchestrator orchestrator;

    // HTTP Mode

    /**
     * A map from service IDs to the sockets of
     * the service wrappers which run those
     * services.
     */
    private final Map<String, HashSet<String>> httpServiceToSockets;

    /**
     * A queue of service wrappers, with the wrapper
     * socket and the last time a request was executed
     * using that service.
     */
    private final PriorityQueue<ServiceWrapperInfo<String>> httpReqDistributionQueue;

    // RabbitMQ Mode

    /**
     * The IDs of all the services that are hosted
     * across all service wrappers connected to
     * the RabbitMQ broker we are connected to.
     */
    private final HashSet<String> rabbitMQServiceIDs;

    /* ---------- CONSTRUCTOR ---------- */

    /**
     * Creates an APIGateway instance with the
     * default config file location.
     */
    public APIGateway() {
        this(new File(DEFAULT_CONFIG_FILE_PATH));
    }

    /**
     * Creates an APIGateway instance with a custom
     * config file location.
     *
     * @param configFile The location of the config file.
     */
    public APIGateway(File configFile) {
        this(configFile, null);
    }

    /**
     * Creates an APIGateway instance with a custom
     * configuration object.
     *
     * @param config The configuration object.
     */
    public APIGateway(APIGatewayConfig config) {
        this(null, config);
    }

    /**
     * Internal constructor that is called by all the
     * public constructors. Supports settings a custom
     * configuration file, but also supports setting a
     * custom configuration object directly.
     *
     * @param configFile The configuration file object.
     * @param config     The configuration object.
     */
    private APIGateway(File configFile, APIGatewayConfig config) {

        if (configFile == null && config == null) {
            throw new RuntimeException("APIGateway must have valid configuration file or configuration object!");
        }

        // Initialize instance variables
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.configFile = configFile;
        this.config = config;
        this.timer = new Timer();

        // Attempt to initialize the configuration
        boolean configInitialized = initializeConfig();
        if (!configInitialized) {
            throw new RuntimeException("Could not initialize configuration!");
        }

        // Initialize internal data structures for keeping
        // track of active services and service wrappers
        if (this.config.reqExecutionProtocol().equals(CommsProtocol.HTTP)) {
            // HTTP Mode
            this.httpServiceToSockets = new HashMap<>();
            this.httpReqDistributionQueue = new PriorityQueue<>();
            this.rabbitMQServiceIDs = null;
        } else if (this.config.reqExecutionProtocol().equals(CommsProtocol.RabbitMQ)) {
            // RabbitMQ Mode
            this.httpServiceToSockets = null;
            this.httpReqDistributionQueue = null;
            this.rabbitMQServiceIDs = new HashSet<>();
        } else {
            // Shouldn't be possible
            throw new RuntimeException("Invalid request execution protocol: " + this.config.reqExecutionProtocol());
        }

        logger.trace("APIGateway instantiated with communication protocol: {}", this.config.reqExecutionProtocol());

    }

    /* ---------- METHODS ---------- */

    /**
     * The application configuration.
     *
     * @return The configuration object which
     * changes the behavior of the API Gateway.
     */
    public APIGatewayConfig getConfig() {
        return this.config;
    }

    /**
     * Checks if we have a service wrapper that
     * runs the service with the given ID.
     *
     * @param microserviceID The ID of the service.
     * @return Whether we have such a service wrapper.
     */
    public boolean haveServiceWithID(String microserviceID) {

        // Depending on whether we are interacting with
        // service wrappers over HTTP or RabbitMQ, change
        // what we check

        boolean haveServiceWithID = false;

        if (this.config.reqExecutionProtocol().equals(CommsProtocol.HTTP)) {

            // Get whether we know of a service wrapper
            // that is running a service with this ID
            synchronized (this.httpServiceToSockets) {
                haveServiceWithID = this.httpServiceToSockets.containsKey(microserviceID);
            }

        } else if (this.config.reqExecutionProtocol().equals(CommsProtocol.RabbitMQ)) {

            // Get whether we know of a service wrapper
            // that is running a service with this ID
            synchronized (this.rabbitMQServiceIDs) {
                haveServiceWithID = this.rabbitMQServiceIDs.contains(microserviceID);
            }

        }

        return haveServiceWithID;

    }

    /**
     * Initialize the configuration object.
     *
     * @return Whether the configuration was
     * initialized successfully.
     */
    private boolean initializeConfig() {

        if (this.config != null) {
            return true;
        }

        // Attempt to create the config.
        this.config = BibleguessrUtilities.getConfigObjFromFile(
                this.configFile,
                APIGatewayConfig.class
        );
        return this.config != null;

    }

    public static void main(String[] args) {
        new APIGateway().start();
    }

    /**
     * Main receive request method. These requests can be
     * received from HTTP, RabbitMQ, or any other communication
     * protocol that is supported. This method will check
     * if the path is fundamentally valid, and if so, attempt
     * to get the microservice ID from it. If we support that
     * microservice, we make a request to a service wrapper
     * that has that microservice, and have it handle the
     * callback. If there is any failure, we call a failure
     * on the callback with the appropriate error code.
     *
     * @param fullPath   The full path of the request, should be in the
     *                   form <code>/microservice-id/request-path</code>
     * @param parameters The parameters of the request.
     * @param callback   The callback to notify of the result.
     */
    public void receiveRequest(String fullPath, Map<String, String> parameters, CommsCallback callback) {

        if (callback == null || fullPath == null || parameters == null) {
            logger.error("Null parameter(s) passed into receiveRequest()!");
            return;
        }

        // Pull out the microservice ID and request path from the full path
        String[] splitPath = fullPath.split("/");

        if (splitPath.length != 3) {
            logger.trace("Received request with invalid path: \"{}\"", fullPath);
            callback.commFailed(StatusCode.HTTP_BAD_URL);
            return;
        }

        String microserviceID = splitPath[1];
        String requestPath = splitPath[2];

        // See if we have a service with this ID
        boolean haveServiceWithID = haveServiceWithID(microserviceID);

        if (!haveServiceWithID) {
            // No service wrapper is running this service,
            // return error code.
            logger.trace("Received a request for service with an ID we don't have: \"{}\"", microserviceID);
            callback.commFailed(StatusCode.NO_MICROSERVICE_WITH_ID);
            return;
        }

        // We know of a microservice with that ID, so it's time to make
        // the request.

        if (this.config.reqExecutionProtocol().equals(CommsProtocol.HTTP)) {

            // Retrieve all service wrappers that have this service

            HashSet<String> wrappersWithService;

            synchronized (this.httpServiceToSockets) {
                wrappersWithService = new HashSet<>(this.httpServiceToSockets.get(microserviceID));
            }

            // Get the one that we haven't used in the longest amount of time

            String wrapperSocket = null;

            synchronized (this.httpReqDistributionQueue) {

                // Get the first service wrapper in the queue
                // that has a config value that is in
                // the wrappersWithService set, remove it
                // from the queue, store its socket, then
                // update its last used time and add it
                // back to the queue.

                LinkedList<ServiceWrapperInfo<String>> wrappersToAddBack = new LinkedList<>();

                while (wrapperSocket == null && !this.httpReqDistributionQueue.isEmpty()) {

                    ServiceWrapperInfo<String> potentialWrapper = this.httpReqDistributionQueue.poll();

                    if (potentialWrapper == null) {
                        // Null wrapper shouldn't even be in here, but whatever
                        continue;
                    }

                    if (wrappersWithService.contains(potentialWrapper.getConfig())) {
                        // Found it!
                        wrapperSocket = potentialWrapper.getConfig();
                        potentialWrapper.updateWhenLastRequestSent();
                    }

                    // Whether this was the one we wanted or not,
                    // put it into the list of wrappers we
                    // want to add back to the queue.
                    wrappersToAddBack.push(potentialWrapper);

                }

                // Add all the wrappers we took out back into the queue
                while (!wrappersToAddBack.isEmpty()) {
                    this.httpReqDistributionQueue.add(wrappersToAddBack.pop());
                }

            }

            if (wrapperSocket == null) {

                // For some reason, we had marked that
                // we know a service wrapper with this
                // service ID, but we don't have any
                // service wrappers that are running
                // that service.

                logger.debug("We know of a service wrapper that is running a service with ID \"{}\", but we don't " +
                        "have any service wrappers that are running that service!", microserviceID);
                callback.commFailed(StatusCode.INTERNAL_ERROR);
                return;

            }

            String[] socketComponents = wrapperSocket.split(":");
            String host = socketComponents[0];

            // Construct URL with http scheme, service wrapper
            // host and port, and service/request identifiers pulled
            // from received URL.
            HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                    .scheme("http")
                    .host(host)
                    .addPathSegment(microserviceID)
                    .addPathSegment(requestPath);

            // If the socket also has a port then set that
            if (socketComponents.length > 1) {
                urlBuilder.port(Integer.parseInt(socketComponents[1]));
            }

            // Add all parameters as query parameters to the URL
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }

            // Make the request
            // TODO if we can't access this service wrapper then try another one and remove this one
            logger.trace("Making HTTP request to URL: {}", urlBuilder.build());
            this.orchestrator.makeHTTPRequest(urlBuilder, callback);

        } else if (this.config.reqExecutionProtocol().equals(CommsProtocol.RabbitMQ)) {

            // Generate a unique ID for this request, since we received
            // it over HTTP it doesn't have one already.
            String uuid = UUID.randomUUID().toString();

            // Create JSON object
            ObjectNode request = GlobalObjectMapper.get().createObjectNode();

            // Put in meta-level parameters like UUID,
            // microservice ID and request path.
            request.put(Request.UUID_PARAMETER_NAME, uuid);
            request.put(this.config.rabbitMQConfig().microserviceIDField(), microserviceID);
            request.put(this.config.rabbitMQConfig().requestPathField(), requestPath);

            // Add all other parameters
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                request.put(entry.getKey(), entry.getValue());
            }

            try {

                // Serialize the JSON object and pass it off to
                // make the actual RabbitMQ request
                byte[] body = GlobalObjectMapper.get().writeValueAsBytes(request);

                logger.trace("Making RabbitMQ Request: {}", new String(body, StandardCharsets.UTF_8));

                this.orchestrator.makeRabbitMQRequest(
                        uuid,
                        body,
                        callback
                );

            } catch (JsonProcessingException e) {
                logger.error("Cannot write JSON object as bytes, dropping request!", e);
                callback.commFailed(StatusCode.INTERNAL_ERROR);
            }

        }

    }

    /**
     * Schedules a task to update the service wrappers
     * and the corresponding services that we have
     * at our disposal.
     */
    private void scheduleDetection() {

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (config.reqExecutionProtocol().equals(CommsProtocol.HTTP)) {

                    // HTTP Mode

                    if (httpServiceToSockets == null || httpReqDistributionQueue == null) {
                        logger.error("Service -> sockets map or request distribution queue is null when request " +
                                "execution protocol is HTTP!");
                        return;
                    }

                    Map<String, HashSet<String>> newServiceToSockets = ServiceWrapperDetector.detectHTTPServiceWrappers(
                            config.httpSockets(),
                            orchestrator
                    );

                    // Construct set of all valid sockets
                    HashSet<String> allValidSockets = new HashSet<>();
                    for (HashSet<String> setOfValidSockets : newServiceToSockets.values()) {
                        allValidSockets.addAll(setOfValidSockets);
                    }

                    // Update service -> sockets map
                    synchronized (httpServiceToSockets) {

                        // Clear out any service IDs that we no
                        // longer have across any of our
                        // service wrappers
                        Set<String> oldServiceIDs = new HashSet<>(httpServiceToSockets.keySet());
                        for (String serviceID : oldServiceIDs) {

                            if (newServiceToSockets.containsKey(serviceID)) {
                                continue;
                            }

                            // Remove from map
                            httpServiceToSockets.remove(serviceID);

                        }

                        // Update map with new sockets, possibly
                        // meaning new service IDs
                        httpServiceToSockets.putAll(newServiceToSockets);

                    }

                    // Update the distribution queue by removing
                    // any service wrappers which are no longer
                    // valid and adding any new ones
                    synchronized (httpReqDistributionQueue) {

                        Iterator<ServiceWrapperInfo<String>> iterator = httpReqDistributionQueue.iterator();
                        Set<String> oldServiceWrapperSockets = new HashSet<>();

                        // First, get rid of all old
                        // service wrappers.
                        while (iterator.hasNext()) {

                            ServiceWrapperInfo<String> info = iterator.next();
                            if (allValidSockets.contains(info.getConfig())) {
                                // Store the socket, we'll need it later
                                oldServiceWrapperSockets.add(info.getConfig());
                            } else {
                                // Remove from list, no longer valid
                                iterator.remove();
                            }

                        }

                        // Next, add all new service wrappers.
                        for (String socket : allValidSockets) {

                            if (oldServiceWrapperSockets.contains(socket)) {
                                // We already have this service wrapper
                                continue;
                            }

                            // Add to list
                            httpReqDistributionQueue.add(new ServiceWrapperInfo<>(socket));

                        }

                    }

                } else if (config.reqExecutionProtocol().equals(CommsProtocol.RabbitMQ)) {

                    // RabbitMQ Mode

                    if (rabbitMQServiceIDs == null) {
                        logger.error("Service ID set is null when request execution protocol is RabbitMQ!");
                        return;
                    }

                    HashSet<String> newServiceIDs = ServiceWrapperDetector.detectRabbitMQServiceWrappers(
                            config.rabbitMQConfig(),
                            orchestrator
                    );

                    // Update service ID set
                    synchronized (rabbitMQServiceIDs) {

                        // Small enough to where it's probably
                        // just faster to do a clear and add all
                        rabbitMQServiceIDs.clear();
                        rabbitMQServiceIDs.addAll(newServiceIDs);

                    }

                }

            }
        }, 0, this.config.wrapperDetectionIntervalInMs());

        logger.info("Scheduled service wrapper detection!");

    }

    /**
     * Shuts down the API Gateway.
     */
    public void shutdown() {

        // Stop updating service wrappers list
        this.timer.cancel();

        // Shutdown all classes managed by the
        // CommsOrchestrator
        if (this.orchestrator != null) {
            this.orchestrator.shutdown();
        }

        logger.info("Shut down the API Gateway!");

    }

    /**
     * Starts the API Gateway by starting up
     * the communications orchestrator and
     * schedules the detection of all the
     * service wrappers which have been
     * configured.
     */
    public void start() {

        // Instantiate the CommsOrchestrator
        this.orchestrator = new CommsOrchestrator(this);

        // Schedule detection of service wrappers
        scheduleDetection();

        logger.info("The API Gateway has initialized!");

    }

}