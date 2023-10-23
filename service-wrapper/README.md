# Service Wrapper

A library which standardizes how Bibleguessr services operate. The service wrapper serves as an intermediary between
service requests/responses (made by the API gateway) and the library business logic. The intention of the library is to
allow for multiple types of service communication: RabbitMQ, GET/POST, and in-code requests. The advantage of this
wrapper is the ability to deploy Bibleguessr services in multiple configurations. RabbitMQ can be used when deploying
Bibleguessr services in scalable clusters, like with Kubernetes. GET/POST can be used when deploying individual
instances of services. In-code requests can be used in resource-constrained environments where all Bibleguessr services
are wrapped in one executable to remove the need for containerized services and/or virtualization.

The Service Wrapper should mandate a service ID from services, so that service context paths can be hosted without
conflict on bundled deployments (service paths look like `/service-id/path`, so that two services can have a path
called `path` and no conflicts arise). Additionally, individual service paths should also be registered with the Service
Wrapper, likely associated with a custom request object, so that requests can be routed much more efficiently.

# Class Structure & Descriptions

- `gg.bibleguessr.service_wrapper`
  - `example_service`
    - `ExampleRequest` - An example request which simply takes in a `msg` String and returns whether the length of the
      string is evenly divisible by 2. Models how to properly extend the `Request` class.
    - `ExampleService` - An example service which offers the `ExampleRequest`. Models how to properly extend
      the `Microservice` class.
  - `intake`
    - `CommsIntake` - An interface that defines functionality that all communications intakes should have in common.
      Communications intakes are classes that receive requests over the network. Mostly exists so that the `IntakeMgr`
      can hold communications intakes in a list and initialize/shut down all intakes in one loop.
    - `HTTPIntake` - Receives GET/POST requests over HTTP by hosting a Vert.x web server.
    - `IntakeMgr` - Manages all `CommsIntake` classes. Receives request from its intakes, verifies the input, and
      attempts to past them off to the `ServiceWrapper` for execution. Initializes intakes.
    - `RabbitMQIntake` - Receives requests over RabbitMQ and publishes responses.
  - `self_service`
    - `GetIDsRequest` - Doesn't have any parameters, but simply returns the IDs of every microservice running in this
      Service Wrapper.
    - `SelfService` - The service which fields `GetIDsRequest`s. The ID of this service doesn't get returned with
      the `GetIDsRequest` response, as it is kind of an internal service.
  - `Microservice` - Abstract class detailing all attributes and functionalities that must be help and fulfilled by a
    microservice that would be registered with the ServiceWrapper.
  - `Request` - Abstract class where a developer can define what parameters they want for a request and details a
    functionality where the Request object can be parsed from a `Map<String, String>`. Requires that a request path be
    provided. Provides other functionality, like unique identification.
  - `Response` - A response to a request. Can be uniquely identifiable. Largely a wrapper for a JSON object.
  - `ServiceWrapper` - The main class. Manages configuration, allows microservices to be registered and unregistered,
    routes request execution to the appropriate microservice and returns the response, and creates and holds
    the `IntakeMgr`.
  - `ServiceWrapperConfig` - Configurable aspects of the Service Wrapper. Allows the user to set the API key to
    authenticate HTTP requests in, allows the user to toggle and configure different communication platforms like HTTP
    and RabbitMQ.
