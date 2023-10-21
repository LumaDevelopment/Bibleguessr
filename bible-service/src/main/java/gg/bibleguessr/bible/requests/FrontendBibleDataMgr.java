package gg.bibleguessr.bible.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.backend_utils.GlobalObjectMapper;
import gg.bibleguessr.bible.Bible;
import gg.bibleguessr.bible.data_structures.Version;
import gg.bibleguessr.bible.versions.BibleVersionMgr;
import gg.bibleguessr.bible.versions.VersionsUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Generates the frontend bible data that we return
 * in response to frontend bible data requests. It
 * is generated once, and then every time it is
 * requested, a new deep copy is made. This is
 * done for two major reasons:<br>
 * 1) The data JSON is large and time-consuming to
 * create, so we only want to do it once.<br>
 * 2) If the request has a UUID, then the
 * Response class automatically injects the
 * UUID into the response JSON. However, if this
 * is done on the returned data, then our
 * bibleData instance variable here is changed.
 */
public class FrontendBibleDataMgr implements VersionsUpdateListener {

    /* ---------- CONSTANTS ---------- */

    /**
     * The logger name for this class.
     */
    public static final String LOGGER_NAME = FrontendBibleDataMgr.class.getSimpleName();

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The logger for this class.
     */
    private final Logger logger;

    /**
     * The Bible version manager, so we can
     * get the list of versions and be
     * notified when the list changes.
     */
    private final BibleVersionMgr bibleVersionMgr;

    /**
     * The GlobalObjectMapper stored as a variable,
     * because we use a lot of JSON in this class.
     */
    private final ObjectMapper objectMapper;

    /**
     * The bible data object (a JSON object).
     * Generated once and then re-used if it
     * is ever needed again.
     */
    private ObjectNode bibleData;

    /* ---------- CONSTRUCTOR ---------- */

    /**
     * Creates a new BibleDataRequestMgr.
     *
     * @param bibleVersionMgr The Bible version manager,
     *                        so we can check what Bible
     *                        versions are available and
     *                        be notified when they change.
     */
    public FrontendBibleDataMgr(BibleVersionMgr bibleVersionMgr) {

        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.bibleVersionMgr = bibleVersionMgr;
        this.objectMapper = GlobalObjectMapper.get();
        this.bibleData = null;

        // Register this object as a listener
        // for Bible version list updates.
        bibleVersionMgr.addListener(this);

    }

    /* ---------- METHODS ---------- */

    /**
     * Adds a new version to the current bible
     * data. Good when a new version is made
     * available, but we don't want to regenerate
     * the entire bible data.
     *
     * @param version The new version to add.
     */
    private void addVersionToData(Version version) {

        // Add version's name to bible names JSON array
        JsonNode bibleNames = this.bibleData.get("bibleNames");

        if (bibleNames == null || !bibleNames.isArray()) {
            logger.error("Bible names property in bible data is invalid!");
            return;
        }

        ((ArrayNode) bibleNames).add(version.getName());

        // Add version book names to Bible book names
        // dictionary
        JsonNode bibleBookNames = this.bibleData.get("bibleBookNames");

        if (bibleBookNames == null || !bibleBookNames.isObject()) {
            logger.error("Bible book names property in bible data is invalid!");
            return;
        }

        // Now that we know the bible book names object
        // is valid, we can construct a JSON array with
        // the new version's book names.
        ArrayNode versionBookNames = this.objectMapper.createArrayNode();

        for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {
            versionBookNames.add(version.getBookNameByIndex(i));
        }

        // Add the new version's book names to the
        // bible book names object.
        ((ObjectNode) bibleBookNames).set(version.getName(), versionBookNames);

        logger.trace("New Bible version {} has been added to bible data!", version.getName());

    }

    /**
     * Sets the bibleData instance variable.
     * This could be considered a dynamic programming
     * solution, where we generate the data once and
     * then re-use it if it is ever needed again.
     */
    private void generateBibleData(Collection<Version> versions) {

        this.bibleData = this.objectMapper.createObjectNode();

        // Get the list of all version names
        ArrayNode bibleNames = this.objectMapper.createArrayNode();

        for (Version version : versions) {
            bibleNames.add(version.getName());
        }

        this.bibleData.set("bibleNames", bibleNames);

        // Construct a map of version name to its book names
        ObjectNode bibleBookNames = this.objectMapper.createObjectNode();

        for (Version version : versions) {

            // Construct an array of version book names
            ArrayNode versionBookNames = this.objectMapper.createArrayNode();

            for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {
                versionBookNames.add(version.getBookNameByIndex(i));
            }

            // Add that to book name map
            bibleBookNames.set(version.getName(), versionBookNames);

        }

        this.bibleData.set("bibleBookNames", bibleBookNames);

        // Construct a data matrix
        ArrayNode dataMatrix = this.objectMapper.createArrayNode();

        // Loop through every book in the verses per chapter 2D array
        for (Integer[] book : Bible.VERSES_PER_CHAPTER) {

            // Create a new JSON array for the book
            ArrayNode bookArray = this.objectMapper.createArrayNode();

            // Dump all values from Java array into JSON array
            for (Integer value : book) {
                bookArray.add(value);
            }

            // Add JSON array to data matrix
            dataMatrix.add(bookArray);

        }

        this.bibleData.set("dataMatrix", dataMatrix);

        logger.debug("Bible data generated successfully!");

    }

    /**
     * Returns a JSON object which contains:<br>
     * - bibleNames - A list of all Bible versions this service has.<br>
     * - bibleBookNames - A map of version name to book names for that version.<br>
     * - dataMatrix - A matrix that contains the number of verses in
     * every chapter of every book of the Bible.
     *
     * @return A JSON object containing the data needed by the front end.
     */
    public ObjectNode getBibleData() {

        if (bibleData == null) {
            generateBibleData(bibleVersionMgr.getVersions());
        }

        // Return a clone, so the original is never modified
        return this.bibleData.deepCopy();

    }

    /**
     * When a new version is added, add it to the
     * bible data.
     *
     * @param version The new version.
     */
    @Override
    public void onNewVersionAdded(Version version) {
        addVersionToData(version);
    }

    /**
     * When the versions list is set to something
     * new, regenerate the bibleData object.
     *
     * @param versions The new list of versions.
     */
    @Override
    public void onVersionsListSet(Collection<Version> versions) {
        generateBibleData(versions);
    }

}
