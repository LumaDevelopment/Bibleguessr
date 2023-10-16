package gg.bibleguessr.bible;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.bible.objs.Version;
import gg.bibleguessr.bible.requests.FrontendBibleDataMgr;
import gg.bibleguessr.bible.requests.FrontendBibleDataRequest;
import gg.bibleguessr.bible.requests.RandomVerseRequest;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import gg.bibleguessr.service_wrapper.ServiceUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Map;

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

    // Bible numerical constants
    public static final int BOOKS_IN_BIBLE = 66;
    public static final int VERSES_IN_BIBLE = 31_102;
    public static final int MAX_CONTEXT_VERSES = 15_550;

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

    /**
     * The class that operates reading text from different
     * Bible versions.
     */
    private BibleReadingMgr bibleReadingMgr;

    /**
     * Versions offered by this service, reported
     * by the bible reading manager. Map from
     * version name to object.
     */
    private Map<String, Version> versions;

    /**
     * The class that manages the creation of data
     * that we send for the frontend about the
     * available versions, their book names, etc.
     */
    private FrontendBibleDataMgr frontendBibleDataMgr;

    /**
     * The object mapper that this class uses to
     * do JSON operations.
     */
    private final ObjectMapper objectMapper;

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
        this.bibleReadingMgr = null;
        this.versions = null;
        this.frontendBibleDataMgr = null;
        this.objectMapper = new ObjectMapper();

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
        ObjectNode responseContent = objectMapper.createObjectNode();

        // Now that we're here, we can do a couple of parameter sanity checks
        String versionName = request.getVersion();

        // Check if the version name is valid.
        if (versionName.isBlank() || !versions.containsKey(versionName)) {
            responseContent.put("error", 0);
            return new Response(responseContent, request.getUUID());
        }

        int numOfContextVerses = request.getNumOfContextVerses();

        // Check if the number of context verses is valid.
        if (numOfContextVerses < 0 || numOfContextVerses > MAX_CONTEXT_VERSES) {
            responseContent.put("error", 1);
            return new Response(responseContent, request.getUUID());
        }

        Version version = versions.get(versionName);

        // TODO for Dan
        // In this case, we're good to go.
        // Steps:
        // 1) Select a random verse.
        // 2) Gather its context verses. Make sure to account for the
        // case where the context would expand beyond the bounds of the
        // Bible. (ex. Genesis 1:1 with >0 context verses)
        // 3) Consult with Michael on how he wants us to handle multiple
        // verses. Does he want multiple verse objects?
        // 4) Whatever the solution, assemble one or more verse JSON
        // objects (can create a new JSON object with
        // objectMapper.createObjectNode()) and fill it with the
        // information Michael needs. Text can be pulled from BibleReadingMgr,
        // book, chapter, and verse information can be pulled from the
        // Bible class, and once you have the Book object from
        // the verse object, you can get its name using
        // version.getBookNameByObject().
        // 5) However you structure these responses, make sure to put
        // information about them in the "Response Parameters"
        // section of the "Random Verse Request" in RequestResponseSpecifications.md
        // 6) Add all verse JSON objects to the responseContent object.

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
            return new Response(frontendBibleDataMgr.getBibleData(), bibleDataReq.getUUID());
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
     * Gets the BibleReadingMgr object for this service.
     *
     * @return The BibleReadingMgr object for this service.
     */
    public BibleReadingMgr getBibleReadingMgr() {
        return bibleReadingMgr;
    }

    /**
     * Attempts to get the Version object of the
     * version with the given name.
     *
     * @param versionName The name of the version to get.
     * @return The Version object of the version with the
     * given name, or <code>null</code> if no such version
     * exists.
     */
    public Version getVersionByName(String versionName) {

        if (versionName == null) {
            return null;
        }

        return versions.get(versionName);

    }

    /**
     * Gets all Bible versions accessible by
     * this service.
     *
     * @return All Bible versions accessible by
     * this service.
     */
    public Collection<Version> getVersions() {
        return versions.values();
    }

    /**
     * Gets the frontend Bible data manager.
     *
     * @return The frontend Bible data manager.
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
        config = ServiceUtilities.getConfigObjFromFile(configFile, BibleServiceConfig.class);

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

        // Initialize the Bible reading manager, and pull
        // all Bible versions available to us after it
        // is done initializing.
        this.bibleReadingMgr = new BibleReadingMgr(
                config.bibleFileExtension(),
                getBibleFiles()
        );
        this.versions = bibleReadingMgr.getBibleVersions();

        // Initialize the frontend Bible data manager
        this.frontendBibleDataMgr = new FrontendBibleDataMgr(this);

        logger.info("Bible service has been initialized!");

    }

    @Override
    public void shutdown() {
        // Nothing to do.
    }

}