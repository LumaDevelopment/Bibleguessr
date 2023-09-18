# Service Wrapper

A library which standardizes how Bibleguessr services operate. The service wrapper serves as an intermediary between service requests/responses and the library business logic. The intention of the library is to allow for multiple types of service communication: RabbitMQ, GET/POST, and in-code requests. The advantage of this wrapper is the ability to deploy Bibleguessr services in multiple configurations. RabbitMQ can be used when deploying Bibleguessr services in scalable clusters, like with Kubernetes. GET/POST can be used when deploying individual instances of services. In-code requests can be used in resource-constrained environments where all Bibleguessr services are wrapped in one executable to remove the need for containerized services and/or virtualization.

The Service Wrapper should mandate a service ID from services, so that service context paths can be hosted without conflict on bundled deployments (service paths look like `service-id/path`, so that two services can have a path called `path` and no conflicts arise). Additionally, individual service paths should also be registered with the Service Wrapper, likely associated with a custom request object, so that requests can be routed much more efficiently.

Usage ideas:
- There is a `Launchpad` class that contains `public static void main(String[] args)` and creates an instance of `ServiceWrapper`.
- Then, you can use the `ServiceWrapper` to run one or more `Microservice` instances.
- The `Microservice` informs the `ServiceWrapper` what it's ID is, and what type of `Request`s it accepts.
- `ServiceWrapper` builds a map of `Request`s to `Microservice`.
- `ServiceWrapper` has a `receiveRequest()` method that accepts a `Request` and returns a `Response`.
- `ServiceWrapper` is passed into Vert.x and RabbitMQ components.

ISSUES:
- Need to define a way where `MainVerticle` can pass of a request to `ServiceWrapper`, which needs to parse it as a `Request`, which needs to pass off to the appropriate `Microservice` instance, which needs to return a `Response`, which needs to be passed back to `MainVerticle` to be sent back to the client.
- The problem is that this `Request` must also be able to be parsed from Vert.x or RabbitMQ without the microservice ever needing to know what Vert.x or RabbitMQ is. Each path is a different Request object though, so maybe we can do something with instantiating a Request class with each parameter? Maybe each Microservice can have a `RequestParser` or something like that that takes in a map of Strings, converts to the appropriate `Request` object, and then returns a response. Or maybe have a `Request.parse()` method that takes in a map of Strings.
