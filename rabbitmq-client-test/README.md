# RabbitMQ Client Test

## Description

Going into this project, none of us had used RabbitMQ before. However, seeing as it was a critical piece of the Service
Wrapper and API Gateway's communication in large scale deployments, it needed to be implemented. So, this project was
developed for testing and learning how a client would interact with our "server-side" RabbitMQ code in the Service
Wrapper. While RabbitMQ is largely a role-agnostic message broker, in our environment, the Service Wrapper could be
considered the server (receives requests, creates responses) and the API Gateway could be considered the client (creates
requests, receives responses).

## Functionality

This test project connects to the local RabbitMQ instance with the default credentials for user authentication, exchange
name, and queue names. The project connects to RabbitMQ, registers a response consumer, and then publishes a request for
the Service Wrapper's example service. To see information about the type of request/response this client deals with,
see [the Service Wrapper's RequestResponseSpecifications.md](../service-wrapper/RequestResponseSpecifications.md).