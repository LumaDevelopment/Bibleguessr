package gg.bibleguessr.api_gateway.comms;

import gg.bibleguessr.backend_utils.CommsCallback;
import okhttp3.HttpUrl;

public class HTTPRequestExecutor {

    private final CommsOrchestrator orchestrator;

    public HTTPRequestExecutor(CommsOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void request(HttpUrl url, CommsCallback callback) {

    }

}
