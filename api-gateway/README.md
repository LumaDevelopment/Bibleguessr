# API Gateway

An application which bridges the front-end and the network of service wrappers that support the overall application. The
API Gateway uses a configured protocol to connect to as many service wrappers as are configured, and distribtues
requests it receives between them equally. Additionally, the API Gateway will collate the capabilities of each service
wrapper, and knows what services it has access to at any given moment.

# Class Structure & Descriptions

- `gg.bibleguessr.api_gateway`
    - `comms`
        - `CommsOrchestrator` - Manages receiving and executing requests across multiple protocols. Interfaces heavily
          with the `APIGateway` class to receive configuration information and to distribute requests to Service
          Wrappers.
        - `CommsProtocol` - An enum representing the different protocols that the API Gateway can use to send requests
          to Service Wrappers. Also the enum used to determine which protocol the API Gateway will use when started.
        - `HTTPRequestExecutor` - Sends requests to Service Wrappers using HTTP and handles the response.
        - `HTTPRequestReceiver` - Receives requests from the frontend for Service Wrapper distribution. Handles
          malformed requests.
        - `RabbitMQRequestExecutor` - Sends requests to Service Wrappers using RabbitMQ and handles the response.
    - `APIGateway` - The main class that interfaces with the `CommsOrchestrator`. Uses the `ServiceWrapperDetector` to
      obtain information about the Service Wrappers it has access to, and distributes requests among them.
    - `APIGatewayConfig` - Configure the behavior of the API Gateway, including the protocol it uses to make requests,
      the port it listens for requests on, the interval at which it scans for service wrappers, the API key it uses to
      authenticate with service wrappers, etc.
    - `ServiceWrapperDetector` - Detects Service Wrappers by contacting all configured Service Wrappers and querying the
      service IDs that they have.
    - `ServiceWrapperInfo` - A class that holds information about a Service Wrapper, including its configuration and the
      last time we made a request to it.