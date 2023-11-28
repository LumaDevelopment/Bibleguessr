package gg.bibleguessr.api_gateway.comms;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.api_gateway.APIGateway;
import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.RabbitMQConfiguration;
import gg.bibleguessr.backend_utils.StatusCode;
import io.vertx.core.Vertx;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommsOrchestrator {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of the logger for this class.
     */
    public static final String LOGGER_NAME = CommsOrchestrator.class.getSimpleName();

    /* ---------- INSTANCE VARIABLES ---------- */

    // Common Variables

    /**
     * The logger for this class.
     */
    private final Logger logger;

    /**
     * The APIGateway instance, used for configuration,
     * as well as to pass received requests up to.
     */
    private final APIGateway apiGateway;

    /**
     * The Vert.x instance, used to deploy the
     * HTTPRequestReceiver.
     */
    private Vertx vertx;

    /**
     * The HTTPRequestReceiver instance, used to
     * receive HTTP requests.
     */
    private HTTPRequestReceiver httpRequestReceiver;

    // HTTP Request Execution Variables

    /**
     * The HTTPRequestExecutor instance, used to
     * execute HTTP requests.
     */
    private final HTTPRequestExecutor httpReqExec;

    // RabbitMQ Request Execution Variables

    /**
     * The RabbitMQRequestExecutor instance, used to
     * execute RabbitMQ requests.
     */
    private final RabbitMQRequestExecutor rabbitMQReqExec;

    /* ---------- CONSTRUCTOR ---------- */

    /**
     * Creates a CommsOrchestrator instance, launches up
     * the HTTP server over which we receive requests,
     * and makes the object used to execute requests over
     * the chosen communication protocol.
     *
     * @param apiGateway The APIGateway instance.
     */
    public CommsOrchestrator(APIGateway apiGateway) {

        if (apiGateway == null) {
            throw new RuntimeException("Cannot create CommsOrchestrator with null APIGateway!");
        }

        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.apiGateway = apiGateway;

        // Set up ability to receive HTTP requests
        // Initializes vertx and httpRequestReceiver
        boolean receiverInitialized = initHTTPRequestReceiver();
        if (receiverInitialized) {
            logger.info("Successfully started HTTP server!");
        } else {
            throw new RuntimeException("Failed to launch HTTP server, cannot receive requests!");
        }

        // Set up request executor
        CommsProtocol requestExecutionProtocol = this.apiGateway.getConfig().reqExecutionProtocol();

        if (requestExecutionProtocol.equals(CommsProtocol.HTTP)) {
            this.httpReqExec = new HTTPRequestExecutor(this);
            this.rabbitMQReqExec = null;
        } else if (requestExecutionProtocol.equals(CommsProtocol.RabbitMQ)) {
            this.httpReqExec = null;
            this.rabbitMQReqExec = new RabbitMQRequestExecutor(this);
        } else {
            this.httpReqExec = null;
            this.rabbitMQReqExec = null;
        }

    }

    /* ---------- METHODS ---------- */

    /**
     * Attempts to create a Vert.x instance and a
     * HTTPRequestReceiver instance, then attempts
     * to deploy the HTTPRequestReceiver instance
     * using Vert.x. Blocks until the deployment
     * finishes successfully or fails.
     *
     * @return Whether the deployment was successful.
     */
    private boolean initHTTPRequestReceiver() {

        if (this.vertx == null) {
            this.vertx = Vertx.vertx();
        }

        if (this.httpRequestReceiver == null) {
            this.httpRequestReceiver = new HTTPRequestReceiver(this);
        }

        // Concurrency handling
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        vertx.deployVerticle(this.httpRequestReceiver).onComplete(res -> {

            if (res.failed()) {
                logger.error("Error while starting server to receive requests!", res.cause());
            }

            // Concurrency handling
            success.set(res.succeeded());
            latch.countDown();

        });

        try {
            // Wait for intake to finish launching
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for HTTP server to start!", e);
            return false;
        }

        return success.get();

    }

    /**
     * Passes HTTP requests off to the HTTP Request Executor.
     *
     * @param urlBuilder The URL to make the request to.
     * @param callback   The callback to call when the request is complete.
     */
    public void makeHTTPRequest(HttpUrl.Builder urlBuilder, CommsCallback callback) {

        if (this.httpReqExec == null) {
            logger.error("Cannot make HTTP request because the executor has not been instantiated!");
            callback.commFailed(StatusCode.INTERNAL_ERROR);
            return;
        }

        this.httpReqExec.request(urlBuilder, callback);

    }

    /**
     * Makes a single response RabbitMQ request.
     *
     * @param uuid     The unique ID of the request.
     * @param body     The JSON request body, serialized into a byte array.
     * @param callback The callback to call when the request is complete.
     */
    public void makeRabbitMQRequest(String uuid, byte[] body, CommsCallback callback) {
        this.rabbitMQReqExec.singleResponseRequest(uuid, body, callback);
    }

    /**
     * Makes a multi response RabbitMQ request.
     *
     * @param uuid The unique ID of the request.
     * @param body The JSON request body, serialized into a byte array.
     * @return A list of responses received from the service wrappers.
     */
    public List<ObjectNode> makeRabbitMQRequest(String uuid, byte[] body) {
        return this.rabbitMQReqExec.multiResponseRequest(uuid, body);
    }

    /**
     * Passes up requests received from the HTTPRequestReceiver
     * to the API Gateway.
     *
     * @param path       The path of the request.
     * @param parameters The parameters of the request.
     * @param callback   The callback to call when we have the request response.
     */
    public void receiveRequest(String path, Map<String, String> parameters, CommsCallback callback) {
        this.apiGateway.receiveRequest(path, parameters, callback);
    }

    /**
     * Shuts down the HTTP server and the Vert.x instance.
     * Also, shuts down the RabbitMQ executor if it exists.
     */
    public void shutdown() {

        vertx.close();

        if (this.rabbitMQReqExec != null) {
            this.rabbitMQReqExec.shutdown();
        }

    }

    /* ---------- CONFIG RETRIEVAL METHODS ---------- */

    /**
     * Gets the allowed CORS origins.
     *
     * @return The allowed CORS origins.
     */
    public List<String> getAllowedCorsOrigins() {
        return this.apiGateway.getConfig().allowedCorsOrigins();
    }

    /**
     * Gets the API key that must be used to authenticate
     * self with service wrappers.
     *
     * @return The API key.
     */
    public String getApiKey() {
        return this.apiGateway.getConfig().apiKey();
    }

    /**
     * Gets the port that the HTTP server should host on.
     * Retrieved from the API Gateway config.
     *
     * @return The port that the HTTP server should host on.
     */
    public int getHTTPRequestReceiverPort() {
        return this.apiGateway.getConfig().port();
    }

    /**
     * Gets the amount of time that must have passed since
     * we last received a response to a multi response request
     * until we conclude we have received all responses.
     *
     * @return The multi-response timeout in milliseconds.
     */
    public long getMultiResponseTimeoutInMs() {
        return this.apiGateway.getConfig().multiResponseTimeoutInMs();
    }

    /**
     * Gets the configuration by which the API gateway
     * can connect to the RabbitMQ broker.
     *
     * @return The RabbitMQ configuration.
     */
    public RabbitMQConfiguration getRabbitMQConfig() {
        return this.apiGateway.getConfig().rabbitMQConfig();
    }

    /**
     * Gets the amount of time that must pass without
     * a response to a single response message before
     * we declare the request has failed.
     *
     * @return The single-response timeout in milliseconds.
     */
    public long getSingleResponseTimeoutInMs() {
        return this.apiGateway.getConfig().singleResponseTimeoutInMs();
    }

}
