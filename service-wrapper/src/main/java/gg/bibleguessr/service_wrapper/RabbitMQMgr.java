package gg.bibleguessr.service_wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.backend_utils.RabbitMQConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all RabbitMQ operations. Notably,
 * connecting to the server, all message handling
 * related tasks, and posting message responses.
 */
public class RabbitMQMgr {

  /* ---------- CONSTANTS ---------- */

  public static final String LOGGER_NAME = RabbitMQMgr.class.getSimpleName();

  /* ---------- VARIABLES ---------- */

  // CORE VARIABLES

  /**
   * Logging variable.
   */
  private final Logger logger;

  /**
   * Service wrapper, used to access the config, check
   * get Microservice objects of microservices that are
   * currently running, and execute requests.
   */
  private final ServiceWrapper serviceWrapper;

  /**
   * All configuration needed to connect to
   * and utilize RabbitMQ.
   */
  private final RabbitMQConfiguration config;

  // RABBITMQ VARIABLES

  /**
   * The RabbitMQ connection. Not really interacted
   * with much after establishing.
   */
  private Connection conn;

  /**
   * The RabbitMQ channel. Kind of like a client. We
   * use this to declare the exchange, the queues,
   * set up the request consumer, and to send
   * responses.
   */
  private Channel channel;

  /**
   * The consumer we use to handle messages. Stored
   * as an instance variable so just in case we need
   * to use it multiple times we're not storing multiple
   * consumer objects.
   */
  private DefaultConsumer consumer;

  // JSON VARIABLES

  /**
   * Jackson writes JSON in UTF-8, so we consider
   * it the charset to use for all RabbitMQ
   * communications.
   */
  private final Charset charset = StandardCharsets.UTF_8;

  // STATUS VARIABLES

  /**
   * Tracks whether RabbitMQ is currently running.
   */
  private boolean running;

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * The one constructor to rule them all.
   *
   * @param serviceWrapper The service wrapper.
   */
  public RabbitMQMgr(ServiceWrapper serviceWrapper) {
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.serviceWrapper = serviceWrapper;
    this.config = serviceWrapper.getConfig().rabbitMQConfig();
    this.conn = null;
    this.running = false;
  }

  /* ---------- METHODS ---------- */

  /**
   * Creates and returns a new connection factory.
   * Uses values from the internal configuration.
   *
   * @return A new connection factory.
   */
  private ConnectionFactory getConnectionFactory() {

    ConnectionFactory factory = new ConnectionFactory();

    // Grab connection credentials and server
    // location from the config.
    factory.setUsername(config.username());
    factory.setPassword(config.password());
    factory.setHost(config.host());
    factory.setPort(config.port());

    // Set the virtual host only if it exists
    String virtualHost = config.virtualHost();
    if (!virtualHost.isBlank()) {
      factory.setVirtualHost(config.virtualHost());
    }

    return factory;

  }

  /**
   * Returns a consumer for the requests queue. Modular for the
   * following scenario: each microservice is configured to
   * have a different requests and response queue name, and
   * multiple microservices are running at once. This means
   * that each microservice needs its own consumer, and each
   * consumer needs to be able to handle requests for all
   * microservices.
   *
   * @return A consumer for the requests queue.
   */
  public DefaultConsumer getRequestConsumer() {

    if (consumer != null) {
      return consumer;
    }

    consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag,
                                 Envelope envelope,
                                 AMQP.BasicProperties properties,
                                 byte[] body)
        throws IOException {

        // Convert byte array to String
        String messageBody = new String(body, charset);
        logger.debug("Received message w/ body: {}", messageBody);

        // Attempt to parse as JSON
        ObjectNode node = GlobalObjectMapper.parseStringAsJSONObject(messageBody);

        // Check if parse was successful.
        if (node == null) {
          logger.error("Received non-JSON message, can't parse.");
          return;
        }

        // Grab delivery tag from message envelope, used
        // to acknowledge or reject a message.
        long deliveryTag = envelope.getDeliveryTag();

        // Tentative JsonNode objects used to check
        // if all the necessary information to execute
        // the Request is present in the message
        JsonNode microserviceIDNode = node.get(config.microserviceIDField());
        JsonNode requestPathNode = node.get(config.requestPathField());

        // Check if both values are present and textual,
        // if not, then acknowledge the message because no
        // other service wrapper will be able to parse this.
        if ((microserviceIDNode == null || !microserviceIDNode.isTextual()) ||
          (requestPathNode == null || !requestPathNode.isTextual())) {
          logger.error("Received message with no microservice ID or request path, can't execute request.");
          channel.basicAck(deliveryTag, false);
          return;
        }

        // Grab service ID and request path from JSON
        String microserviceID = microserviceIDNode.asText();
        String requestPath = requestPathNode.asText();

        // Attempt to retrieve microservice from service wrapper.
        Microservice microservice = serviceWrapper.getRunningMicroservice(microserviceID);

        if (microservice == null) {
          // We're not running the microservice this request is for.
          // No big deal, just reject the message and
          // let the right microservice handle it.
          channel.basicReject(deliveryTag, true);
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
          channel.basicAck(deliveryTag, false);
          return;
        }

        // Standardize all fields of the JSON object into
        // a String -> String map
        Map<String, String> parameters = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {

          Map.Entry<String, JsonNode> entry = it.next();

          if (entry.getKey().equals("deliveryTag") ||
            entry.getKey().equals(config.microserviceIDField()) ||
            entry.getKey().equals(config.requestPathField())) {
            // Entries that are not of relevancy to
            // the Request parser.
            continue;
          }

          // Store the key-value pair in the parameters map
          parameters.put(entry.getKey(), entry.getValue().asText());

        }

        // Create new request object from the correct type
        // and the standardized parameters.
        Request request = Request.parse(requestClazz, parameters);

        if (request == null) {
          // Request parsing failed, log error and acknowledge.
          logger.error("Failed to parse request!");
          channel.basicAck(deliveryTag, false);
          return;
        }

        // Execute the request
        Response response = serviceWrapper.executeRequest(request);

        // We somehow failed to execute the request.
        // Leave it in the queue and hope someone else picks
        // it up and does it better.
        if (response == null) {
          logger.error("Failed to execute request!");
          channel.basicReject(deliveryTag, true);
          return;
        }

        // Acknowledge the message, we've got a
        // valid response to send back.
        channel.basicAck(deliveryTag, false);

        // Publish response to responses queue
        channel.basicPublish(
          config.exchangeName(),
          config.responsesQueue(),
          null,
          GlobalObjectMapper.get().writeValueAsBytes(response.toJSONNode())
        );

      }
    };

    return consumer;

  }

  /**
   * Returns whether the RabbitMQ client is running.
   *
   * @return Whether the RabbitMQ client is running.
   */
  public boolean isRunning() {
    return running;
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

    // If a connection is not established,
    // then establish it.
    if (conn == null) {

      ConnectionFactory factory = getConnectionFactory();

      // Attempt to establish a new connection.
      // If it fails, report failure and set
      // conn to null.
      try {
        conn = factory.newConnection();
      } catch (Exception e) {
        conn = null;
        String exceptionName = e.getClass().getSimpleName();
        logger.error(exceptionName + " while trying to create a new RabbitMQ connection!", e);
        return false;
      }

      // Attempt to create a new channel from
      // our existing connection. If it fails,
      // report failure and set both channel
      // and connection to null.
      try {
        channel = conn.createChannel();
      } catch (IOException e) {
        conn = null;
        channel = null;
        logger.error("IOException while trying to create a new RabbitMQ channel!", e);
        return false;
      }

    }

    try {

      // First, declare the exchange
      channel.exchangeDeclare(config.exchangeName(), "direct", true);

      // Bind to the responses queue
      // We do this first because the moment we receive
      // a request we need to be able to publish
      // a response.
      channel.queueDeclare(config.responsesQueue(), false, false, false, null);
      channel.queueBind(config.responsesQueue(), config.exchangeName(), config.responsesQueue());

      // Bind to the requests queue
      channel.queueDeclare(config.requestsQueue(), false, false, false, null);
      channel.queueBind(config.requestsQueue(), config.exchangeName(), config.requestsQueue());

      // Not sure if this has to be uniquer per-wrapper
      // or per queue, but we don't use it later, so I
      // figured I'd just make it random.
      String consumerTag = UUID.randomUUID().toString();

      // Set up a consumer for the requests queue
      channel.basicConsume(config.requestsQueue(), false, consumerTag, getRequestConsumer());

      logger.info("Successfully setup RabbitMQ!");
      return true;

    } catch (Exception e) {
      conn = null;
      channel = null;
      String exceptionName = e.getClass().getSimpleName();
      logger.error(exceptionName + " while trying to setup RabbitMQ after establishing a connection!", e);
      return false;
    }

  }

  /**
   * Stops the RabbitMQ client (closing all connections)
   * and sets the running status to false.
   */
  public void stop() {

    // Each statement is in a separate
    // try catch because we want the
    // function to continue even if
    // either of the two methods throw
    // an error.

    try {
      channel.close();
    } catch (Exception e) {
      // Ignore.
    }

    try {
      conn.close();
    } catch (Exception e) {
      // Ignore.
    }

    running = false;

  }

}
