package gg.bibleguessr.bible;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.backend_utils.BibleguessrUtilities;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.bible.data_structures.Verse;
import gg.bibleguessr.bible.data_structures.Version;
import gg.bibleguessr.bible.requests.FrontendBibleDataMgr;
import gg.bibleguessr.bible.requests.FrontendBibleDataRequest;
import gg.bibleguessr.bible.requests.RandomVerseRequest;
import gg.bibleguessr.bible.versions.BibleVersionMgr;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

/**
 * The BibleService class is the main class for the
 * Bible microservice.
 */
public class BibleService extends Microservice {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of the logger used by this class
     * (the simple name of this class).
     */
    public static final String LOGGER_NAME = BibleService.class.getSimpleName();

    /**
     * The default path to the config file.
     */
    public static final String DEFAULT_CONFIG_FILE_PATH = "bible_service_config.json";

    /* ---------- VARIABLES ---------- */

    /**
     * The logger this class uses to record its
     * status and errors.
     */
    private final Logger logger;

    /**
     * The config file for this service.
     */
    private final File configFile;

    /**
     * The config object for this service.
     */
    private BibleServiceConfig config;

    // Managers

    /**
     * The class that manages all Bible versions
     * that are available to this service.
     */
    private BibleVersionMgr bibleVersionMgr;

    /**
     * The class that operates reading text from different
     * Bible versions.
     */
    private BibleTextMgr bibleTextMgr;

    /**
     * The class that manages the data that the front end
     * needs to operate.
     */
    private FrontendBibleDataMgr frontendBibleDataMgr;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Creates a new BibleService object with the
     * default config file path. Calls BibleService(configFile).
     */
    public BibleService() {
        this(new File(DEFAULT_CONFIG_FILE_PATH));
    }

    /**
     * Creates a new BibleService object with the
     * specified config file. Initializes the service, which
     * includes managing the config file and Bible versions.
     *
     * @param configFile The config file for this service.
     */
    public BibleService(File configFile) {

        // Tell the Service Wrapper that the ID
        // of this service is "bible"
        super("bible");

        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.configFile = configFile;
        this.config = null;
        this.bibleVersionMgr = null;
        this.bibleTextMgr = null;
        this.frontendBibleDataMgr = null;

        initializeService();

    }


    /* ---------- METHODS ---------- */

    /**
     * Executes the given random verse request.
     *
     * @param request The request to execute.
     * @return The response to the request.
     */
    public Response executeRandomVerseRequest(RandomVerseRequest request) {

        // Node to insert response data into
        ObjectNode responseContent = GlobalObjectMapper.get().createObjectNode();

        // Now that we're here, we can do a couple of parameter sanity checks
        String versionName = request.getVersion();
        Version version = bibleVersionMgr.getVersionByName(versionName);
        int numOfContextVerses = request.getNumOfContextVerses();

        // Sanity checks are done in the parse() method of RandomVerseRequest

        // Michael has stated that for simplicity's sake, he will request new verse objects
        // one at a time, meaning there won't be a need (for now) to create an additional
        // JSON object. If in the future he would like multiple verse objects, the
        // current agreed-upon structure would be a JSON object containing an array of
        // Verse JSON objects.

        // Initialize new Random object
        Random rand = new Random();

        // Obtain random index and start and end indices depending on context length
        int randomIndex = rand.nextInt(Bible.NUM_OF_VERSES);
        int startIndex = randomIndex - numOfContextVerses;
        int endIndex = randomIndex + numOfContextVerses;

        // Adjusts the range depending on if the start or end indices fall out of bounds
        if (startIndex < 0) {

            int addToEnd = -startIndex;
            startIndex = 0;
            endIndex += addToEnd;

            // If the shifting put the end index out of bounds
            if (endIndex > Bible.NUM_OF_VERSES - 1) {
                endIndex = Bible.NUM_OF_VERSES - 1;
            }

        } else if (endIndex > Bible.NUM_OF_VERSES - 1) {

            int addToStart = endIndex - (Bible.NUM_OF_VERSES - 1);
            endIndex = Bible.NUM_OF_VERSES - 1;
            startIndex -= addToStart;

            // If the shifting put the start index out of bounds
            if (startIndex < 0) {
                startIndex = 0;
            }

        }

        // Fill the verse array with objects that represent every verse
        // (including the context verse), and store the index of the
        // random verse within the array.
        ArrayNode verseArray = GlobalObjectMapper.get().createArrayNode();
        int randomLocalIndex = -1;

        // Add each piece of text to the context array and set the verse index when appropriate
        for (int i = startIndex; i <= endIndex; i++) {

            ObjectNode verseJSON = GlobalObjectMapper.get().createObjectNode();
            Verse verseObj = Bible.getInstance().getVerseByUniversalIndex(i);

            verseJSON.put("universalIndex", i);
            verseJSON.put("book", version.getBookNameByObject(verseObj.chapter().book()));
            verseJSON.put("chapter", verseObj.chapter().number());
            verseJSON.put("verse", verseObj.number());
            verseJSON.put("text", bibleTextMgr.getVerseText(version, i));

            if (i == randomIndex) {
                // This is the random verse, all other
                // verses are context
                randomLocalIndex = i - startIndex;
            }

            verseArray.add(verseJSON);

        }

        // Put all requested information into the JSON object
        responseContent.put("bibleVersion", versionName);
        responseContent.set("verseArray", verseArray);
        responseContent.put("localVerseIndex", randomLocalIndex);

        return new Response(responseContent, request.getUUID());

    }

    /**
     * Handle all request types that are directed to this service.
     * Should only receive types that are stated in initializeRequestTypesMap().
     *
     * @param request The request to execute.
     * @return The response to the request.
     */
    @Override
    public Response executeRequest(Request request) {

        if (request instanceof FrontendBibleDataRequest bibleDataReq) {
            return new Response(
                    frontendBibleDataMgr.getBibleData(),
                    bibleDataReq.getUUID()
            );
        } else if (request instanceof RandomVerseRequest randomVerseReq) {
            return executeRandomVerseRequest(randomVerseReq);
        } else {
            // Unknown type of request
            logger.error("Received request of unknown type: {}!", request.getClass().getSimpleName());
            return null;
        }

    }

    /**
     * Opens the specified bibles directory and retrieves
     * all valid Bible files.
     *
     * @return Array of Bible files, or <code>null</code>
     * if there was an issue creating/accessing the directory.
     */
    public File[] getBibleFiles() {

        // Establish the directory and attempt to
        // create it if it doesn't exist
        File biblesDirectory = new File(config.biblesDirectory());
        boolean dirCreated = biblesDirectory.mkdirs();

        if (!dirCreated && !biblesDirectory.exists()) {
            // Directory couldn't be created
            logger.error("Could not create bibles directory!");
            return null;
        }

        // Create a filter to determine what files are valid
        FilenameFilter bibleFileFilter =
                (dir, name) -> name.endsWith("." + config.bibleFileExtension());

        // Get all valid bible files in the directory
        return biblesDirectory.listFiles(bibleFileFilter);

    }

    /**
     * Gets the config object for this service.
     *
     * @return The config object for this service.
     */
    public BibleServiceConfig getConfig() {
        return config;
    }

    /**
     * Gets the BibleVersionMgr object for this service.
     *
     * @return The BibleVersionMgr object for this service.
     */
    public BibleVersionMgr getBibleVersionMgr() {
        return bibleVersionMgr;
    }

    /**
     * Gets the BibleReadingMgr object for this service.
     *
     * @return The BibleReadingMgr object for this service.
     */
    public BibleTextMgr getBibleTextMgr() {
        return bibleTextMgr;
    }

    /**
     * Gets the FrontendBibleDataMgr object for this service.
     *
     * @return The FrontendBibleDataMgr object for this service.
     */
    public FrontendBibleDataMgr getFrontendBibleDataMgr() {
        return frontendBibleDataMgr;
    }

    /**
     * Checks if the config object already exists,
     * and if so, returns it. Otherwise, attempts
     * to read the config file and create the
     * config object. Additionally, if the config
     * file doesn't exist and the config class has
     * a getDefault() method, it will be called
     * and the default config will be written to
     * the config file.
     *
     * @return Whether the config now is set
     * correctly.
     */
    public boolean initializeConfig() {

        if (config != null) {
            return true;
        }

        // Attempt to create the config.
        config = BibleguessrUtilities.getConfigObjFromFile(configFile, BibleServiceConfig.class);

        return config != null;

    }

    /**
     * Initialize all request types that
     * this service can handle.
     */
    @Override
    public void initializeRequestTypesMap() {
        initializeRequestType(FrontendBibleDataRequest.class);
        initializeRequestType(RandomVerseRequest.class);
    }

    /**
     * Initialize all aspects of the Bible service, including
     * the configuration, the bible files, the bible reading
     * manager, the frontend bible data manager, etc.
     */
    private void initializeService() {

        // Attempt to initialize the configuration,
        // and throw an error if we can't
        if (!initializeConfig()) {
            throw new RuntimeException(
                    "Configuration file does not exist, see logs for more information.");
        }

        // Get all valid Bible files
        File[] bibleFiles = getBibleFiles();

        if (bibleFiles == null) {
            throw new RuntimeException(
                    "Could not get Bible files, see logs for more information.");
        }

        // Initializes the Bible version manager.
        this.bibleVersionMgr = new BibleVersionMgr();

        // Initialize the Bible reading manager, and pull
        // all Bible versions available to us after it
        // is done initializing.
        this.bibleTextMgr = new BibleTextMgr(
                bibleVersionMgr,
                config.bibleFileExtension(),
                bibleFiles
        );

        // Initialize the frontend Bible data manager.
        this.frontendBibleDataMgr = new FrontendBibleDataMgr(bibleVersionMgr);

        logger.info("Bible service has been initialized!");

    }

    @Override
    public void shutdown() {
        // Nothing to do.
    }

}