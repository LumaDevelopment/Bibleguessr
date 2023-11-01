package gg.bibleguessr.api_gateway.comms;

import gg.bibleguessr.backend_utils.CommsCallback;
import gg.bibleguessr.backend_utils.StatusCode;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPRequestExecutor {

    public static final String LOGGER_NAME = HTTPRequestExecutor.class.getSimpleName();

    private final Logger logger;
    private final CommsOrchestrator orchestrator;
    private final OkHttpClient client;

    public HTTPRequestExecutor(CommsOrchestrator orchestrator) {
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.orchestrator = orchestrator;
        this.client = new OkHttpClient();
    }

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
