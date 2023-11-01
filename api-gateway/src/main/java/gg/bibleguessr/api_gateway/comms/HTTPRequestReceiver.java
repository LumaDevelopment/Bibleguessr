package gg.bibleguessr.api_gateway.comms;

import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.StatusCode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequestReceiver extends AbstractVerticle {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of this class's logger, which
     * is the name of this class.
     */
    public static final String LOGGER_NAME = HTTPRequestReceiver.class.getSimpleName();

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The logger for this class.
     */
    private final Logger logger;

    /**
     * The CommsOrchestrator instance. We pass up received
     * requests up to this class, and it notifies us when
     * it has a response.
     */
    private final CommsOrchestrator orchestrator;

    /**
     * The port that this web server should
     * be hosted on.
     */
    private final int port;

    /* ---------- CONSTRUCTOR ---------- */

    /**
     * Creates an instance of HTTPRequestReceiver. Designed
     * to be dependent on the CommsOrchestrator. Retrieves
     * hosting port from the CommsOrchestrator and is deployed
     * by it.
     *
     * @param orchestrator The CommsOrchestrator instance.
     */
    public HTTPRequestReceiver(CommsOrchestrator orchestrator) {
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.orchestrator = orchestrator;
        this.port = this.orchestrator.getHTTPRequestReceiverPort();
    }

    /* ---------- METHODS ---------- */

    /**
     * Starts this Verticle on the port defined in the configuration.
     * Passes up requests to CommsOrchestrator, which passes them
     * up to APIGateway. Depending on the result, this server
     * will either return response JSON or an error code.
     *
     * @param startPromise a promise which should be called when verticle start-up is complete.
     */
    @Override
    public void start(Promise<Void> startPromise) {

        // Create new HttpServer and Router
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Add a route to avoid CORS issues
        router.route().handler(CorsHandler.create()
                .addOrigins(this.orchestrator.getAllowedCorsOrigins())
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type")
        );

        // Handle requests
        router.route().handler(ctx -> {

            HttpServerRequest req = ctx.request();

            // Load parameters into map
            Map<String, String> parameters = new HashMap<>();
            req.params().forEach(entry -> parameters.put(entry.getKey(), entry.getValue()));

            // Decide what to do based on the response
            CommsCallback callback = new CommsCallback() {

                @Override
                public void onSuccess(String content) {
                    // Deliver JSON string as response to request
                    req.response()
                            .putHeader("content-type", "application/json")
                            .end(content);
                }

                @Override
                public void onFailure(StatusCode errorCode) {
                    // Throw response code
                    throwErrorCode(req, errorCode);
                }

            };

            // Attempt to execute the request
            this.orchestrator.receiveRequest(req.path(), parameters, callback);

        });

        // Start listening with the Router as a request handler
        server.requestHandler(router).listen(port, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                logger.info("Vert.x launched successfully on port {}!", port);
            } else {
                // Failure will be logged in ServiceWrapper
                startPromise.fail(http.cause());
            }
        });

    }

    /**
     * Ends a request/session that a client has open with us
     * with the given error code.
     *
     * @param request   The request to respond to.
     * @param errorCode The error code to respond with.
     */
    public void throwErrorCode(HttpServerRequest request, StatusCode errorCode) {

        if (request == null) {
            return;
        }

        request.response().setStatusCode(errorCode.getStatusCode()).end();

    }

}
