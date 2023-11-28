package gg.bibleguessr.api_gateway.comms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;
import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.backend_utils.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes RabbitMQ requests. This is used to dispatch requests to
 * service wrappers, and to detect what services belong to each
 * configured service wrapper.
 */
public class RabbitMQRequestExecutor {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of the logger for this class.
     */
    public static final String LOGGER_NAME = RabbitMQRequestExecutor.class.getSimpleName();

    /* ---------- INSTANCE VARIABLES ---------- */

    // Core variables

    /**
     * The CommsOrchestrator instance. We use this to
     * access configuration variables.
     */
    private final CommsOrchestrator orchestrator;

    /**
     * The logger for this class.
     */
    private final Logger logger;

    // RabbitMQ variables

    /**
     * Our connection to the RabbitMQ broker.
     */
    private Connection conn;

    /**
     * Our channel, where we manage sending and
     * receiving messages, as well as declaring
     * exchanges and queues.
     */
    private Channel channel;

    // Response tracking

    /**
     * Map from request UUID to response callback.
     * When a single response request is made, a callback
     * is added to this map, and when the response consumer
     * receives a response with a UUID in the list, it calls
     * the callback.
     */
    private final Map<String, SingleResponseCallback> singleResponses;

    /**
     * Map from request UUID to list of responses.
     * When a multi response request is made, a blank
     * list is added to this map, and when the response
     * consumer receives a response with a UUID in the
     * list, it adds the response to the list.
     */
    private final Map<String, LinkedList<String>> multiResponses;

    // JSON variables

    /**
     * Jackson writes JSON in UTF-8, so we consider
     * it the charset to use for all RabbitMQ
     * communications.
     */
    private final Charset charset = StandardCharsets.UTF_8;

    /* ---------- CONSTRUCTOR ---------- */

    /**
     * Creates a new instance of RabbitMQRequestExecutor.
     *
     * @param orchestrator The CommsOrchestrator instance.
     */
    public RabbitMQRequestExecutor(CommsOrchestrator orchestrator) {

        this.orchestrator = orchestrator;
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);

        this.conn = null;
        this.channel = null;

        this.singleResponses = new HashMap<>();
        this.multiResponses = new HashMap<>();

        init();

    }

    /* ---------- METHODS ---------- */

    /**
     * Attempts to initialize the RabbitMQ connection,
     * including establishing exchanges and queues, as
     * well as setting up a response consumer that
     * will feed into our single and multi response
     * maps.
     */
    private void init() {

        ConnectionFactory factory = new ConnectionFactory();

        // Set factory properties
        factory.setUsername(orchestrator.getRabbitMQConfig().username());
        factory.setPassword(orchestrator.getRabbitMQConfig().password());
        factory.setHost(orchestrator.getRabbitMQConfig().host());
        factory.setPort(orchestrator.getRabbitMQConfig().port());

        // Set virtual host, only if it is defined
        if (!orchestrator.getRabbitMQConfig().virtualHost().isBlank()) {
            factory.setVirtualHost(orchestrator.getRabbitMQConfig().virtualHost());
        }

        // Create the connection
        try {
            this.conn = factory.newConnection();
        } catch (Exception e) {
            logger.error("Cannot create RabbitMQ connection.", e);
            this.conn = null;
            return;
        }

        // Create the channel from the connection
        try {
            this.channel = conn.createChannel();
        } catch (Exception e) {
            logger.error("Cannot create RabbitMQ channel.", e);
            this.conn = null;
            this.channel = null;
            return;
        }

        // Retrieve the exchange name and queue names from the RabbitMQ config
        String exchangeName = orchestrator.getRabbitMQConfig().exchangeName();
        String responsesQueue = orchestrator.getRabbitMQConfig().responsesQueue();
        String requestsQueue = orchestrator.getRabbitMQConfig().requestsQueue();

        try {

            // First, declare the exchange
            channel.exchangeDeclare(exchangeName, "direct", true);

            // Bind to the responses queue
            channel.queueDeclare(responsesQueue, false, false, false, null);
            channel.queueBind(responsesQueue, exchangeName, responsesQueue);

            // Bind to the requests queue
            channel.queueDeclare(requestsQueue, false, false, false, null);
            channel.queueBind(requestsQueue, exchangeName, requestsQueue);

            // Generate a consumer tag for the responses queue, doesn't
            // need to be preserved
            String consumerTag = UUID.randomUUID().toString();

            // Set up a consumer for the responses queue
            channel.basicConsume(responsesQueue, false, consumerTag, new DefaultConsumer(channel) {

                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {

                    String uuid = null;

                    // Convert the message body to JSON object
                    ObjectNode objectNode = GlobalObjectMapper.parseBytesAsJSONObject(body);

                    if (objectNode != null) {
                        JsonNode jsonNode = objectNode.get("uuid");
                        if (jsonNode != null && jsonNode.isTextual()) {
                            uuid = jsonNode.asText();
                        }
                    }

                    if (uuid == null) {
                        logger.error("Received malformed response from service wrapper: {}", new String(body, charset));
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        return;
                    }

                    // If the UUID is valid and is a key in the single responses
                    // map or multi responses map, then either called the
                    // SingleResponseCallback or add the

                    synchronized (singleResponses) {

                        SingleResponseCallback src = singleResponses.remove(uuid);

                        if (src != null) {

                            src.onResponse(new String(body, charset));
                            channel.basicAck(envelope.getDeliveryTag(), false);

                            // Don't need to proceed if we've found a single response
                            // callback, since we've already called it
                            return;

                        }

                    }

                    synchronized (multiResponses) {

                        LinkedList<String> responses = multiResponses.get(uuid);

                        if (responses == null) {
                            channel.basicReject(envelope.getDeliveryTag(), true);
                            return;
                        }

                        responses.push(new String(body, charset));
                        channel.basicAck(envelope.getDeliveryTag(), false);

                    }

                }

            });

        } catch (Exception e) {

            logger.error("Cannot establish exchange or queues.", e);

            try {
                this.channel.abort();
            } catch (Exception e2) {
                // Don't care
            }
            this.channel = null;

            try {
                this.conn.abort();
            } catch (Exception e2) {
                // Don't care
            }
            this.conn = null;

        }

    }

    /**
     * Sends a request that is expected to receive only one
     * response. How long we wait for said response is defined
     * within the configuration.
     *
     * @param uuid     The unique identifier of the request.
     * @param body     The JSON request body, serialized into a byte array.
     * @param callback The callback that will be called when the request
     *                 either fails or succeeds.
     */
    public void singleResponseRequest(String uuid, byte[] body, CommsCallback callback) {

        if (this.channel == null || this.conn == null) {
            // Connection was not made successfully
            callback.commFailed(StatusCode.INTERNAL_ERROR);
            return;
        }

        // Track whether the callback has been called
        AtomicBoolean callbackCalled = new AtomicBoolean(false);

        // Setup concurrency variable to be triggered
        // by the single response callback
        CountDownLatch latch = new CountDownLatch(1);

        // Create a callback that will be called when
        // the response consumer receives a response
        // with the given UUID
        SingleResponseCallback src = response -> {

            synchronized (callbackCalled) {

                if (callbackCalled.get()) {
                    return;
                }

                callback.commSucceeded(response);
                callbackCalled.set(true);

            }

            latch.countDown();

        };

        // Insert UUID into responses map
        synchronized (this.singleResponses) {
            this.singleResponses.put(uuid, src);
        }

        // Publish a request
        try {

            channel.basicPublish(
                    orchestrator.getRabbitMQConfig().exchangeName(),
                    orchestrator.getRabbitMQConfig().requestsQueue(),
                    null,
                    body
            );

        } catch (IOException e) {

            logger.error("Unable to publish to the requests queue.", e);

            synchronized (callbackCalled) {

                if (callbackCalled.get()) {
                    return;
                }

                callback.commFailed(StatusCode.INTERNAL_ERROR);
                callbackCalled.set(true);

            }

            return;
        }

        // Await on the CountDownLatch
        try {

            boolean receivedResponse = latch.await(orchestrator.getSingleResponseTimeoutInMs(), TimeUnit.MILLISECONDS);

            if (!receivedResponse) {

                logger.error("Timed out while waiting for response to request with UUID: {}", uuid);

                synchronized (callbackCalled) {

                    if (callbackCalled.get()) {
                        return;
                    }

                    callback.commFailed(StatusCode.INTERNAL_ERROR);
                    callbackCalled.set(true);

                }

            }

        } catch (InterruptedException e) {

            logger.error("Interrupted while waiting for a response.");

            synchronized (callbackCalled) {

                if (callbackCalled.get()) {
                    return;
                }

                callback.commFailed(StatusCode.INTERNAL_ERROR);
                callbackCalled.set(true);

            }

        }

    }

    /**
     * Sends a RabbitMQ request which is expected to receive multiple
     * responses. How long we wait for said responses is defined
     * within the configuration.
     *
     * @param uuid The unique identifier of the request.
     * @param body The JSON request body, serialized into a byte array.
     * @return The (hopefully) multiple responses, represented as JSON.
     */
    public List<ObjectNode> multiResponseRequest(String uuid, byte[] body) {

        if (this.channel == null || this.conn == null) {
            // Connection was not made successfully
            return new LinkedList<>();
        }

        // Add an empty LinkedList to the map, associated
        // with this UUID
        synchronized (multiResponses) {
            this.multiResponses.put(uuid, new LinkedList<>());
        }

        // Attempt to publish the actual request
        try {

            channel.basicPublish(
                    orchestrator.getRabbitMQConfig().exchangeName(),
                    orchestrator.getRabbitMQConfig().requestsQueue(),
                    null,
                    body
            );

        } catch (IOException e) {

            logger.error("Unable to publish to the requests queue.", e);

            synchronized (multiResponses) {
                this.multiResponses.remove(uuid);
            }

            return new LinkedList<>();

        }

        // Track whether we've stopped receiving responses,
        // and wait until we stop receiving responses.
        CountDownLatch latch = new CountDownLatch(1);

        // Set up a timer to check if we've stopped receiving
        // responses
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            int lastNumOfResponses = 0;

            @Override
            public void run() {

                List<String> responses;

                synchronized (multiResponses) {
                    responses = multiResponses.get(uuid);
                }

                // There is no responses list corresponding to this UUID
                if (responses == null) {
                    logger.warn("Responses list already removed from multi responses map, " +
                            "even though the timer is still running.");
                    latch.countDown();
                    timer.cancel();
                    return;
                }

                if (responses.size() == lastNumOfResponses) {
                    // We have received no more responses.
                    latch.countDown();
                    timer.cancel();
                } else {
                    lastNumOfResponses = responses.size();
                }

            }

        }, orchestrator.getMultiResponseTimeoutInMs(), orchestrator.getMultiResponseTimeoutInMs());

        // Wait until the TimerTask stops
        try {
            latch.await();
        } catch (InterruptedException e) {

            logger.error("Interrupted while waiting for responses to multi response request.", e);

            synchronized (multiResponses) {
                this.multiResponses.remove(uuid);
            }

            return new LinkedList<>();

        }

        // Pull responses from the map
        LinkedList<String> responseStrings;

        synchronized (multiResponses) {
            responseStrings = this.multiResponses.remove(uuid);
        }

        // Parse all response strings into JSON objects
        LinkedList<ObjectNode> responses = new LinkedList<>();

        while (!responseStrings.isEmpty()) {

            String responseString = responseStrings.pop();
            ObjectNode response = GlobalObjectMapper.parseStringAsJSONObject(responseString);

            if (response != null) {
                responses.push(response);
            }

        }

        return responses;

    }

    /**
     * Shuts down all RabbitMQ communications by closing
     * the RabbitMQ channel and connection.
     */
    public void shutdown() {

        try {
            this.channel.close();
        } catch (Exception e) {
            // Don't care
        }
        this.channel = null;

        try {
            this.conn.close();
        } catch (Exception e) {
            // Don't care
        }
        this.conn = null;

        this.singleResponses.clear();
        this.multiResponses.clear();

    }

    /* ---------- INNER CLASSES ---------- */

    /**
     * Callback so that single response requests
     * can be notified when a response is received.
     */
    interface SingleResponseCallback {
        void onResponse(String response);
    }

}
