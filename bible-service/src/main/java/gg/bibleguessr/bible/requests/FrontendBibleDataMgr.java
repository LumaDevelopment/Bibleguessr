package gg.bibleguessr.bible.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.bible.Bible;
import gg.bibleguessr.bible.BibleService;
import gg.bibleguessr.bible.objs.Version;

public class FrontendBibleDataMgr {

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The BibleService object, so we can access
     * the different versions we have.
     */
    private final BibleService service;

    /**
     * ObjectMapper, because we use a lot of
     * JSON in this class.
     */
    private final ObjectMapper objectMapper;

    /**
     * The bible data object (a JSON object).
     * Generated once and then re-used if it
     * is ever needed again.
     */
    private ObjectNode bibleData;

    /* ---------- CONSTRUCTOR ---------- */

    public FrontendBibleDataMgr(BibleService service) {
        this.service = service;
        this.objectMapper = new ObjectMapper();
        this.bibleData = null;
    }

    /* ---------- METHODS ---------- */

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

        if (bibleData != null) {
            return bibleData;
        }

        this.bibleData = this.objectMapper.createObjectNode();

        // Get the list of all version names
        ArrayNode bibleNames = this.objectMapper.createArrayNode();

        for (Version version : service.getVersions()) {
            bibleNames.add(version.getName());
        }

        this.bibleData.set("bibleNames", bibleNames);

        // Construct a map of version name to its book names
        ObjectNode bibleBookNames = this.objectMapper.createObjectNode();

        for (Version version : service.getVersions()) {

            // Construct an array of version book names
            ArrayNode versionBookNames = this.objectMapper.createArrayNode();

            for (int i = 0; i < BibleService.BOOKS_IN_BIBLE; i++) {
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

        return this.bibleData;

    }

}
