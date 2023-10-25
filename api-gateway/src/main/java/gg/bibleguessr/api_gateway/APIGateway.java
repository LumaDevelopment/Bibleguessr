package gg.bibleguessr.api_gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class APIGateway {

    /* ---------- CONSTANTS ---------- */

    public static final String LOGGER_NAME = APIGateway.class.getSimpleName();

    /* ---------- INSTANCE VARIABLES ---------- */

    private final Logger logger;

    /**
     * Used for scheduling service wrapper detection.
     */
    private final Timer timer;

    /* ---------- CONSTRUCTOR ---------- */

    public APIGateway() {
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.timer = new Timer();
    }

    /* ---------- METHODS ---------- */

    public static void main(String[] args) {
        // new APIGateway();
    }

}