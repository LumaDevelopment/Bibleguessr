package gg.bibleguessr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.UUID;

public class Main {

    public static final String USERNAME = "guest";
    public static final String PASSWORD = "guest";
    public static final String HOST = "localhost";
    public static final int PORT = 5672;

    public static final String EXCHANGE_NAME = "exchange";
    public static final String REQUESTS_QUEUE = "requests";
    public static final String RESPONSES_QUEUE = "responses";

    public static void main(String[] args) {

        try {

            System.out.println("Starting RabbitMQ...");
            long startTime = System.currentTimeMillis();

            ConnectionFactory factory = new ConnectionFactory();

            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            factory.setHost(HOST);
            factory.setPort(PORT);

            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            // First declare the exchange
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

            // Bind to the responses queue
            channel.queueDeclare(RESPONSES_QUEUE, false, false, false, null);
            channel.queueBind(RESPONSES_QUEUE, EXCHANGE_NAME, RESPONSES_QUEUE);

            // Bind to the requests queue
            channel.queueDeclare(REQUESTS_QUEUE, false, false, false, null);
            channel.queueBind(REQUESTS_QUEUE, EXCHANGE_NAME, REQUESTS_QUEUE);

            // Track UUIDs of interest
            LinkedList<String> uuidsOfInterest = new LinkedList<>();

            // Set up a consumer for the responses queue
            Charset charset = StandardCharsets.UTF_8;
            String consumerTag = UUID.randomUUID().toString();

            channel.basicConsume(RESPONSES_QUEUE, false, consumerTag,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)
                                throws IOException {

                            // Convert byte array to String
                            String messageBody = new String(body, charset);
                            System.out.println("Received message w/ body: " + messageBody);

                            // Attempt to parse as JSON
                            ObjectNode node = parseStringAsJSONObject(messageBody);

                            if (node == null) {
                                System.err.println("Received non-JSON message, can't parse to determine UUID.");
                                return;
                            }

                            JsonNode uuidNode = node.get("uuid");

                            if (uuidNode == null || !uuidNode.isTextual()) {
                                System.err.println("Received JSON message without UUID field.");
                                return;
                            }

                            String uuid = uuidNode.asText();

                            if (uuidsOfInterest.contains(uuid)) {
                                System.out.println("Received message with UUID of interest: " + uuid);
                                uuidsOfInterest.remove(uuid);
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } else {
                                System.out.println("Received message with UUID not of interest: " + uuid);
                                channel.basicReject(envelope.getDeliveryTag(), true);
                            }

                        }
                    });

            long endTime = System.currentTimeMillis();
            System.out.println("RabbitMQ started in " + (endTime - startTime) + "ms");

            System.out.println("Assembling request JSON...");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode request = mapper.createObjectNode();

            String requestUUID = UUID.randomUUID().toString();
            uuidsOfInterest.add(requestUUID);
            System.out.println("Request will have UUID: " + requestUUID);

            request.put("uuid", requestUUID);
            request.put("microservice_id", "example-service");
            request.put("request_path", "example-request");

            // Change this for different results
            String msg = "Hello, World!";
            System.out.println("Message has length " + msg.length() + ", lengthDivisibleBy2 should return: " + (msg.length() % 2 == 0));
            request.put("msg", msg);

            System.out.println("Done assembling request JSON, publishing.");

            channel.basicPublish(
                    EXCHANGE_NAME,
                    REQUESTS_QUEUE,
                    null,
                    mapper.writeValueAsBytes(request)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

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
    public static ObjectNode parseStringAsJSONObject(String message) {

        ObjectMapper mapper = new ObjectMapper();

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

}