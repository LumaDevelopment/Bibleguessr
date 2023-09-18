package gg.bibleguessr.service_wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class RabbitMQMgr {

  /* ---------- CONSTANTS ---------- */

  public static final String LOGGER_NAME = RabbitMQMgr.class.getSimpleName();

  /* ---------- VARIABLES ---------- */

  // CORE VARIABLES

  private final Logger logger;
  private final ServiceWrapper serviceWrapper;

  /**
   * Actual RabbitMQ client that allows us to
   * communicate with the RabbitMQ server.
   */
  private RabbitMQClient client;
  private final ObjectMapper mapper;

  // STATUS VARIABLES
  private boolean running;

  /* ---------- CONSTRUCTORS ---------- */

  public RabbitMQMgr(ServiceWrapper serviceWrapper) {
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.serviceWrapper = serviceWrapper;
    this.client = null;
    this.mapper = new ObjectMapper();
    this.running = false;
  }

  /* ---------- METHODS ---------- */

  /**
   * Returns whether the RabbitMQ client is running.
   *
   * @return Whether the RabbitMQ client is running.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Attempts to parse the given message as a JSON object.
   * If this fails at any point along the process, null is
   * returned.
   *
   * @param message The message to parse.
   * @return The parsed message as a JSON object, or null if
   * the message could not be parsed.
   */
  public ObjectNode parseStringAsJSONObject(String message) {

    try {
      JsonNode jsonNode = mapper.readTree(message);
      if (jsonNode.isObject()) {
        return (ObjectNode) jsonNode;
      }
    } catch (Exception e) {
      // Ignore.
    }

    return null;

  }

  /**
   * Attempts to start the RabbitMQ client.
   * If the client is already started, or if the
   * client starts successfully, this method returns
   * true. Otherwise, this method returns false.
   * Blocks until connection is complete.
   *
   * @return Whether the RabbitMQ client is started
   */
  public boolean start() {

    if (running) {
      // Running successfully already
      return true;
    }

    ServiceWrapperConfig config = serviceWrapper.getConfig();

    if (client == null) {

      // Load options from configuration
      RabbitMQOptions options = new RabbitMQOptions();
      options.setUser(config.rabbitMQUsername());
      options.setPassword(config.rabbitMQPassword());
      options.setHost(config.rabbitMQHost());
      options.setPort(config.rabbitMQPort());

      // Initialize client
      client = RabbitMQClient.create(serviceWrapper.getVertx(), options);

    }

    // Concurrency handling
    CountDownLatch latch = new CountDownLatch(1);
    AtomicBoolean success = new AtomicBoolean(false);

    client.addConnectionEstablishedCallback(promise -> {
      client.exchangeDeclare(config.rabbitMQExchangeName(), "direct", true, false)

        // Bind to the responses queue
        .compose(v -> client.queueDeclare(config.rabbitMQResponsesQueue(), false, false, false))
        .compose(declareOk -> client.queueBind(config.rabbitMQResponsesQueue(), config.rabbitMQExchangeName(), config.rabbitMQResponsesQueue()))

        // Bind to the requests queue
        .compose(v -> client.queueDeclare(config.rabbitMQRequestsQueue(), false, false, false))
        .compose(declareOk -> client.queueBind(config.rabbitMQRequestsQueue(), config.rabbitMQExchangeName(), config.rabbitMQRequestsQueue()))

        // Set up the ability to consume from the requests queue
        .compose(v -> client.basicConsumer(config.rabbitMQResponsesQueue(), new QueueOptions().setAutoAck(false)))
        .compose(consumer -> {
          consumer.handler(message -> {

            // Get message body
            String messageBody = message.body().toString();
            logger.trace("Received message w/ body: {}", messageBody);

            // Attempt to parse as JSON
            ObjectNode node = parseStringAsJSONObject(messageBody);

            if (node == null) {
              logger.error("Received non-JSON message, can't parse to determine delivery tag.");
              return;
            }

            // Grab delivery tag from message.
            long deliveryTag = message.envelope().getDeliveryTag();

            // Tentative JsonNode objects used to check
            // if all the necessary information to execute
            // the Request is present in the message
            JsonNode microserviceIDNode = node.get(config.rabbitMQMicroserviceIDField());
            JsonNode requestPathNode = node.get(config.rabbitMQRequestPathField());

            // Grab service ID and request path from JSON
            if ((microserviceIDNode == null || !microserviceIDNode.isTextual()) ||
              (requestPathNode == null || !requestPathNode.isTextual())) {
              logger.error("Received message with no microservice ID or request path, can't execute request.");
              client.basicAck(deliveryTag, false);
              return;
            }

            String microserviceID = microserviceIDNode.asText();
            String requestPath = requestPathNode.asText();

            // Attempt to retrieve microservice
            // from service wrapper.
            Microservice microservice = serviceWrapper.getRunningMicroservice(microserviceID);

            if (microservice == null) {
              // We're not running the microservice this request is for.
              // No big deal, just don't acknowledge the message and
              // let the right microservice handle it.
              return;
            }

            // Attempt to retrieve request class
            // from the microservice.
            Class<? extends Request> requestClazz = microservice.getRequestTypeFromPath(requestPath);

            if (requestClazz == null) {
              // A request path has been requested for a
              // request type that doesn't exist. Log
              // error and acknowledge.
              logger.error("Received request for existing microservice with path that does not exist!");
              client.basicAck(deliveryTag, false);
              return;
            }

            // Parse all fields of the JSON object into
            // a String -> String map
            Map<String, String> parameters = new HashMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {

              Map.Entry<String, JsonNode> entry = it.next();

              if (entry.getKey().equals("deliveryTag") ||
                entry.getKey().equals(config.rabbitMQMicroserviceIDField()) ||
                entry.getKey().equals(config.rabbitMQRequestPathField())) {
                // Entries that are not of relevancy to
                // the Request parser.
                continue;
              }

              // Store the key-value pair in the parameters map
              parameters.put(entry.getKey(), entry.getValue().asText());

            }

            Request request = Request.parse(requestClazz, parameters);

            if (request == null) {
              // Request parsing failed, log error and acknowledge.
              logger.error("Failed to parse request!");
              client.basicAck(deliveryTag, false);
              return;
            }

            // Execute the request
            Response response = serviceWrapper.executeRequest(request);

            // We somehow failed to execute the request.
            // Leave it in the queue and hope someone else picks
            // it up and does it better.
            if (response == null) {
              logger.error("Failed to execute request!");
              return;
            }

            // Define basic properties for the response
            BasicProperties properties = new AMQP.BasicProperties.Builder()
              .build();

            Buffer messageBuffer;

            try {
              messageBuffer = Buffer.buffer(mapper.writeValueAsBytes(response.toJSONNode()));
            } catch (JsonProcessingException e) {
              logger.error("Could not create buffer from response JSON!", e);
              return;
            }

            // Acknowledge the message
            client.basicAck(deliveryTag, false);

            // Publish response to responses queue
            client.basicPublishWithDeliveryTag(
              config.rabbitMQExchangeName(),
              config.rabbitMQResponsesQueue(),
              properties,
              messageBuffer,
              event -> {} // don't need to do anything here
            );

          });

          return Future.succeededFuture(consumer);

        })
        .onComplete(asyncResultHandler -> {

          if (asyncResultHandler.succeeded()) {
            // Mark the client as running
            success.set(true);
          } else {
            // Failure, log it, then continue
            logger.error("Failed to set up a consumer for the requests queue!", asyncResultHandler.cause());
          }

          latch.countDown();

        });

    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      logger.error("Interrupted while waiting for RabbitMQ client to start!");
      return false;
    }

    if (success.get()) {
      running = true;
      logger.info("RabbitMQ client started successfully!");
    }

    return success.get();

  }

  /**
   * Stops the RabbitMQ client (closing all connections)
   * and sets the running status to false.
   */
  public void stop() {
    client.stop();
    running = false;
  }

}
