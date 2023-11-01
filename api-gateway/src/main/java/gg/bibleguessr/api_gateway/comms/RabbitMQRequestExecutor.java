package gg.bibleguessr.api_gateway.comms;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.backend_utils.CommsCallback;

import java.util.List;

public class RabbitMQRequestExecutor {

    private final CommsOrchestrator orchestrator;

    public RabbitMQRequestExecutor(CommsOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public void singleResponseRequest(String uuid, byte[] body, CommsCallback callback) {

    }

    public List<ObjectNode> multiResponseRequest(String uuid, byte[] body) {
        return null;
    }

}
