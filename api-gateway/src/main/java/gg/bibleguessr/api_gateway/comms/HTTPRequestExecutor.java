package gg.bibleguessr.api_gateway.comms;

import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.StatusCode;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes HTTP requests for other classes. Primarily, this
 * is used to get request responses from service wrappers.
 */
public class HTTPRequestExecutor {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of the logger for this class.
     */
    public static final String LOGGER_NAME = HTTPRequestExecutor.class.getSimpleName();

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The logger for this class.
     */
    private final Logger logger;

    /**
     * The CommsOrchestrator instance. We use this to
     * get the API key to inject into requests.
     */
    private final CommsOrchestrator orchestrator;

    /**
     * The OkHttpClient instance, used to execute
     * HTTP requests.
     */
    private final OkHttpClient client;

    /* ---------- CONSTRUCTOR ---------- */

    /**
     * Creates an instance of HTTPRequestExecutor.
     * This class is intentionally dependent on the
     * CommsOrchestrator.
     *
     * @param orchestrator The CommsOrchestrator instance.
     */
    public HTTPRequestExecutor(CommsOrchestrator orchestrator) {
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.orchestrator = orchestrator;
        this.client = new OkHttpClient();
    }

    /* ---------- METHODS ---------- */

    /**
     * Executes the given HTTP request and calls
     * the given callback with the response.
     *
     * @param urlBuilder The incomplete URL. The reason
     *                   this method takes in a builder
     *                   is so it can inject the API
     *                   key if that is necessary.
     * @param callback   The callback to call with the
     *                   response.
     */
    public void request(HttpUrl.Builder urlBuilder, CommsCallback callback) {

        // Inject API key
        if (!orchestrator.getApiKey().isBlank()) {
            urlBuilder.addQueryParameter("apiKey", orchestrator.getApiKey());
        }

        Request request = new Request.Builder()
                .addHeader("accept", "application/json")
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (response.code() != 200) {

                StatusCode code = StatusCode.fromInt(response.code());

                if (code == null) {
                    logger.error("Received unexpected error code from server: {}", response.code());
                    callback.commFailed(StatusCode.INTERNAL_ERROR);
                } else {
                    callback.commFailed(code);
                }

            } else {

                if (response.body() != null) {
                    // Mark this request as succeeded
                    callback.commSucceeded(response.body().string());
                } else {
                    logger.error("Received OK code from server, but response body was null!");
                    callback.commFailed(StatusCode.INTERNAL_ERROR);
                }

            }

        } catch (Exception e) {
            logger.error("Encountered an error while attempting to execute an HTTP request!", e);
            callback.commFailed(StatusCode.INTERNAL_ERROR);
        }

    }

}
