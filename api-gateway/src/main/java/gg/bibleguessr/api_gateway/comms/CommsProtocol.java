package gg.bibleguessr.api_gateway.comms;

/**
 * All the communication protocols that we can use
 * to connect to service wrappers and execute
 * requests with them. These are NOT the protocols
 * over which we receive requests from the outside.
 * The only protocol over which we do that is HTTP.
 */
public enum CommsProtocol {
    HTTP,
    RabbitMQ
}
