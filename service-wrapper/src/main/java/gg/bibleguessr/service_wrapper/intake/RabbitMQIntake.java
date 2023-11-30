package gg.bibleguessr.service_wrapper.intake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;
import gg.bibleguessr.backend_utils.*;
import gg.bibleguessr.service_wrapper.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all RabbitMQ operations. Notably,
 * connecting to the server, all message handling
 * related tasks, and posting message responses.
 */
public class RabbitMQIntake implements CommsIntake {

  /* ---------- CONSTANTS ---------- */

  /**
   * The name of the logger for this class.
   */
  public static final String LOGGER_NAME = RabbitMQIntake.class.getSimpleName();

  /* ---------- VARIABLES ---------- */

  // CORE VARIABLES

  /**
   * Logging variable.
   */
  private final Logger logger;

  /**
   * Intake manager, handles the path validation, request
   * execution, communications callback calling, etc.
   */
  private final IntakeMgr intakeMgr;

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

  /* ---------- CONSTRUCTORS ---------- */

  /**
   * The one constructor to rule them all.
   *
   * @param intakeMgr The intake manager.
   * @param config    All RabbitMQ configuration variables.
   */
  public RabbitMQIntake(IntakeMgr intakeMgr, RabbitMQConfiguration config) {
    this.logger = LoggerFactory.getLogger(LOGGER_NAME);
    this.intakeMgr = intakeMgr;
    this.config = config;
    this.conn = null;
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

        // Grab delivery tag from message envelope, used
        // to acknowledge or reject a message.
        long deliveryTag = envelope.getDeliveryTag();

        // Attempt to parse as JSON
        ObjectNode node = GlobalObjectMapper.parseStringAsJSONObject(messageBody);

        // Check if parse was successful. If not,
        // remove it from the queue.
        if (node == null) {
          logger.error("Received non-JSON message, can't parse, removing from queue.");
          channel.basicAck(deliveryTag, false);
          return;
        }

        // Try to pull the UUID of the request
        // from the JSON object. If the request
        // has no UUID, remove it from the
        // queue.
        JsonNode uuidNode = node.get(Request.UUID_PARAMETER_NAME);

        if (uuidNode == null || !uuidNode.isTextual()) {
          logger.error("Received unidentifiable request, removing from queue.");
          channel.basicAck(deliveryTag, false);
          return;
        }

        String uuid = uuidNode.asText();

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
          sendErrorResponse(uuid, StatusCode.MALFORMED_REQUEST);
          channel.basicAck(deliveryTag, false);
          return;
        }

        // Grab service ID and request path from JSON
        String microserviceID = microserviceIDNode.asText();
        String requestPath = requestPathNode.asText();

        // Construct path and parameters map to give to IntakeMgr
        String fullPath = "/" + microserviceID + "/" + requestPath;
        Map<String, String> parameters = BibleguessrUtilities.convertObjNodeToStringMap(node);
        parameters.remove(config.microserviceIDField());
        parameters.remove(config.requestPathField());

        // Determine what we'll do when we receive the response
        CommsCallback callback = new CommsCallback() {
          @Override
          public void onSuccess(String content) {
            // Publish response to responses queue
            try {

              channel.basicAck(deliveryTag, false);

              channel.basicPublish(
                config.exchangeName(),
                config.responsesQueue(),
                null,
                content.getBytes(charset)
              );

            } catch (Exception e) {
              logger.error("Could not acknowledge request and/or publish response!", e);
            }
          }

          @Override
          public void onFailure(StatusCode errorCode) {

            // Error encountered

            try {
              if (errorCode.equals(StatusCode.NO_MICROSERVICE_WITH_ID) || errorCode.equals(StatusCode.INTERNAL_ERROR)) {
                // Send it back into the queue for some other
                // Service Wrapper to deal with
                channel.basicReject(deliveryTag, true);
              } else {
                // Send error response and
                // acknowledge request
                sendErrorResponse(uuid, errorCode);
                channel.basicAck(deliveryTag, false);
              }
            } catch (IOException e) {
              logger.error("Could not reject/acknowledge request after being unable to fulfill it!", e);
            }

          }

        };

        // Send the request off to the intake manager
        long startTime = System.currentTimeMillis();
        intakeMgr.receiveRequest(fullPath, parameters, callback);
        long endTime = System.currentTimeMillis();
        logger.trace("Intake manager handled request in {} ms.", endTime - startTime);

      }
    };

    return consumer;

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
  @Override
  public boolean initialize() {

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
   * Send a message in the responses queue with the given
   * request ID and the given status code as an error
   * code. This notifies whoever sent the request that
   * the request was not fulfilled, and why.
   *
   * @param uuid      The UUID of the request.
   * @param errorCode The error code to send.
   */
  private void sendErrorResponse(String uuid, StatusCode errorCode) {

    if (uuid == null || errorCode == null) {
      logger.error("Null uuid and/or error given while attempting to send error response!");
      return;
    }

    ObjectNode response = GlobalObjectMapper.get().createObjectNode();

    // Put in uuid and error code
    response.put(Request.UUID_PARAMETER_NAME, uuid);
    response.put("error", errorCode.getStatusCode());

    // Publish response to responses queue
    try {
      channel.basicPublish(
        config.exchangeName(),
        config.responsesQueue(),
        null,
        GlobalObjectMapper.get().writeValueAsBytes(response)
      );
    } catch (Exception e) {
      logger.error("Encountered error while attempting to send error response! Ironic...", e);
    }

  }

  /**
   * Stops the RabbitMQ client (closing all connections)
   * and sets the running status to false.
   */
  @Override
  public void shutdown() {

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

  }

}
