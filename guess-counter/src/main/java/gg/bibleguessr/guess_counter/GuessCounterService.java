package gg.bibleguessr.guess_counter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.backend_utils.BibleguessrUtilities;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.guess_counter.requests.GetCountRequest;
import gg.bibleguessr.guess_counter.requests.IncrementCountRequest;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * A service that keeps track of how many times a
 * guess has been made on a Bibleguessr instance.
 * Note that this was a quickly made service, and for
 * multiple instances of it to be used effectively it
 * would have to be converted to use a central database
 * instead of a local text file.
 */
public class GuessCounterService extends Microservice {

    /* ---------- CONSTANTS ---------- */

    /**
     * Name of the logger for this class.
     */
    public static final String LOGGER_NAME = GuessCounterService.class.getSimpleName();

    /**
     * Default path to the configuration file for this
     * service.
     */
    public static final String DEFAULT_CONFIG_FILE_PATH = "guess_counter_config.json";

    /* ---------- VARIABLES ---------- */

    // Logging and configuration variables

    /**
     * Logger for this class.
     */
    private final Logger logger;

    /**
     * The configuration for this service, which
     * contains the interval at which to update the
     * counter file, as well as the name of the
     * counter file.
     */
    private final GuessCounterServiceConfig config;

    // Counter management variables

    /**
     * Manages writing to and reading from
     * the counter file in a concurrency
     * safe manner.
     */
    private final CounterFileMgr counterFileMgr;

    /**
     * The count of guesses that is saved in the
     * counter file.
     */
    private long fileCount;

    /**
     * The count of guesses that is saved in memory
     * and will be added to the count in the counter
     * file soon.
     */
    private long volatileCount;

    /**
     * Ensures exclusive access to the count variables.
     */
    private final Semaphore countMutex;

    // Counter update variables

    /**
     * Schedules writing to the counter file
     * and updating the count variables.
     */
    private final Timer timer;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Create an instance of the Guess Counter
     * service with the default configuration
     * file path.
     */
    public GuessCounterService() {
        this(new File(DEFAULT_CONFIG_FILE_PATH));
    }

    /**
     * Create an instance of the Guess Counter
     * service with a custom configuration
     * file path.
     */
    public GuessCounterService(File configFile) {

        // The ID of this service
        super("guess-counter");

        // Set up logging and configuration
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.config = BibleguessrUtilities.getConfigObjFromFile(configFile, GuessCounterServiceConfig.class);

        if (this.config == null) {
            throw new RuntimeException("Could not get configuration object from defined config file.");
        }

        // Set up persistent counter storage
        this.counterFileMgr = new CounterFileMgr(this.config.counterFileName());

        this.fileCount = counterFileMgr.getCounter();
        this.volatileCount = 0;
        this.countMutex = new Semaphore(1);

        // Schedule writing the counter to persistent storage
        // and updating the count variables
        this.timer = new Timer();
        scheduleCounterFileUpdate();

    }

    /* ---------- METHODS ---------- */

    /**
     * Handle requests to get the count and increment it.
     *
     * @param request The request to execute.
     * @return The response to the request, or <code>null</code>
     * if there is some unknown/internal execution error.
     */
    @Override
    public Response executeRequest(Request request) {

        if (request instanceof GetCountRequest gcReq) {

            // Node to insert response data into
            ObjectNode responseContent = GlobalObjectMapper.get().createObjectNode();

            synchronized (countMutex) {

                // Get exclusive access to the
                // count variables before reading them
                try {
                    countMutex.acquire();
                } catch (Exception e) {
                    logger.error("Interrupted while waiting for exclusive access to the count variables while " +
                            "executing a GetCountRequest!", e);
                    return null;
                }

                // Create a response count variable within
                // the exclusion zone.
                long responseCount = fileCount + volatileCount;

                // Release exclusive access
                // to the variables
                countMutex.release();

                // Place it in the JSON object outside the
                // exclusion zone, because the JSON is only
                // being accessed by this thread.
                responseContent.put("count", responseCount);

            }

            return new Response(responseContent, request.getUUID());

        } else if (request instanceof IncrementCountRequest gcReq) {

            synchronized (countMutex) {

                // Get exclusive access to the
                // count variables before updating them
                try {
                    countMutex.acquire();
                } catch (Exception e) {
                    logger.error("Interrupted while waiting for exclusive access to the count variables while " +
                            "executing an IncrementCountRequest!", e);
                    return null;
                }

                // Super duper complex.
                volatileCount++;

                // Release exclusive access
                // to the variables
                countMutex.release();

            }

            // Return empty content, because if there was an issue,
            // it would've been communicated as an HTTP error code.
            return new Response(GlobalObjectMapper.get().createObjectNode(), request.getUUID());

        } else {
            // Unknown type of request
            logger.error("Received unknown request type: " + request.getClass().getSimpleName());
            return null;
        }

    }

    @Override
    public void initializeRequestTypesMap() {
        initializeRequestType(GetCountRequest.class);
        initializeRequestType(IncrementCountRequest.class);
    }

    /**
     * Schedule a task that sums the existing count
     * of guesses in the counter file with the
     * volatile count in memory, and writes that
     * new value to the counter file. Additionally,
     * sets that sum as the new file count and
     * resets the volatile count to 0.
     */
    private void scheduleCounterFileUpdate() {

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                synchronized (countMutex) {

                    // Get exclusive access to the
                    // count variables before updating them
                    try {
                        countMutex.acquire();
                    } catch (Exception e) {
                        logger.error("Interrupted while waiting for exclusive access to the count variables within the " +
                                "counter write task!", e);
                        return;
                    }

                    // Calculate the new value that should
                    // be saved to the file
                    long newValue = fileCount + volatileCount;

                    // Update the value in the file
                    counterFileMgr.setCounter(newValue);

                    // Reset the volatile count to 0
                    // and update the persistent/file count
                    fileCount = newValue;
                    volatileCount = 0;

                    // Release exclusive access
                    // to the variables
                    countMutex.release();

                }

            }
        }, this.config.writeIntervalInMs(), this.config.writeIntervalInMs());

    }

    /**
     * Shuts down scheduled task, attempts to save
     * the volatile count, and shuts down the
     * counter file manager.
     */
    @Override
    public void shutdown() {
        this.timer.cancel();
        this.counterFileMgr.setCounter(this.fileCount + this.volatileCount);
        this.counterFileMgr.shutdown();
    }

}