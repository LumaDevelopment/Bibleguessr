package gg.bibleguessr.bible;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gg.bibleguessr.bible.data_structures.Verse;
import gg.bibleguessr.bible.data_structures.Version;
import gg.bibleguessr.bible.requests.FrontendBibleDataMgr;
import gg.bibleguessr.bible.requests.FrontendBibleDataRequest;
import gg.bibleguessr.bible.requests.RandomVerseRequest;
import gg.bibleguessr.bible.versions.BibleVersionMgr;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import gg.bibleguessr.service_wrapper.ServiceUtilities;
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

    // Bible numerical constants

    /**
     * The number of books in the Bible, as
     * far as we're concerned.
     */
    public static final int BOOKS_IN_BIBLE = 66;

    /**
     * The number of verses in the Bible. Depending
     * on what version you read, this number may
     * be lower, but we operate with the highest
     * number possible for maximum compatability.
     */
    public static final int VERSES_IN_BIBLE = 31_102;

    /**
     * The maximum number of verses that can be
     * given to any one verse as context. This
     * number is derived with this equation:<br>
     * <code>floor((VERSES_IN_BIBLE - 1) / 2)</code>
     */
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
        this.bibleVersionMgr = null;
        this.bibleTextMgr = null;
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
        Version version = bibleVersionMgr.getVersionByName(versionName);

        // Check if the version name is valid.
        if (versionName.isBlank() || version == null) {
            responseContent.put("error", 0);
            return new Response(responseContent, request.getUUID());
        }

        int numOfContextVerses = request.getNumOfContextVerses();

        // Check if the number of context verses is valid.
        if (numOfContextVerses < 0 || numOfContextVerses > MAX_CONTEXT_VERSES) {
            responseContent.put("error", 1);
            return new Response(responseContent, request.getUUID());
        }

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
        
        // Michael has stated that for simplicity's sake, he will request new verse objects
        // one at a time, meaning there won't be a need (for now) to create an additional
        // JSON object. If in the future he would like multiple verse objects, the
        // current agreed-upon structure would be a JSON object containing an array of
        // Verse JSON objects.

        // Initialize new Random object
        Random rand = new Random();

        // Obtain random index and start and end indices depending on context length
        int randomIndex = rand.nextInt(VERSES_IN_BIBLE);
        int startIndex = randomIndex - numOfContextVerses;
        int endIndex = randomIndex + numOfContextVerses;

        // Adjusts the range depending on if the start or end indices fall out of bounds
        if (startIndex < 0) {
            startIndex = 0; 
            endIndex += Math.abs(startIndex);
        } else if (endIndex > VERSES_IN_BIBLE - 1) {
            endIndex = VERSES_IN_BIBLE - 1;
            startIndex -= endIndex - (VERSES_IN_BIBLE - 1);
        }

        // Obtain the random verse and the random verse with context as an array and define the random verse index
        String randomText = bibleTextMgr.getVerseText(version, randomIndex);
        
        // -- Experimentation with earlier implementations of this method; ignore --
        //String randomTextWithContext = bibleTextMgr.getPassageText(version, startIndex, endIndex);
        //String[] contextArray = new String[2*numOfContextVerses+1];

        // Define the contextArray and the index of the selected verse
        ArrayNode contextArray = objectMapper.createArrayNode();
        int randomLocalIndex = -1;

        // Add each piece of text to the context array and set the verse index when appropriate
        for (int i = startIndex; i <= endIndex; i++){
            String currentText = bibleTextMgr.getVerseText(version, i);
            contextArray.add(currentText);
            if (currentText.equals(randomText)) randomLocalIndex = i-startIndex;
        }

        // Retrieve the verse information from the Bible instance
        Bible bible = Bible.getInstance();
        Verse verseInfo = bible.getVerseByUniversalIndex(randomIndex);

        // Retrieve the book name, chapter number, and verse number from the verse object
        String bookName = version.getBookNameByObject(verseInfo.chapter().book());
        int chapter = verseInfo.chapter().number();
        int verseNum = verseInfo.number();
        
        // Put all requested information into the JSON object
        responseContent.put("bibleVersion", versionName);
        responseContent.put("bookName", bookName);
        responseContent.put("chapter", chapter);
        responseContent.put("verseNumber", verseNum);
        responseContent.set("verseArray", contextArray);
        responseContent.put("localVerseIndex", randomLocalIndex);
        responseContent.put("globalVerseIndex", randomIndex);
        
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

        // Initializes the Bible version manager.
        this.bibleVersionMgr = new BibleVersionMgr();

        // Initialize the Bible reading manager, and pull
        // all Bible versions available to us after it
        // is done initializing.
        this.bibleTextMgr = new BibleTextMgr(
              bibleVersionMgr,
              config.bibleFileExtension(),
              getBibleFiles()
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