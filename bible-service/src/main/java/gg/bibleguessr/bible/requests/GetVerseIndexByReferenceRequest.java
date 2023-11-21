package gg.bibleguessr.bible.requests;

import gg.bibleguessr.service_wrapper.Request;

import java.util.Map;

public class GetVerseIndexByReferenceRequest extends Request {

    /* ---------- CONSTANTS ---------- */

    /**
     * The path of this request.
     */
    public static final String REQUEST_PATH = "index-by-reference";

    /* ---------- VARIABLES ---------- */

    /**
     * The index of the book of the verse.
     */
    private int bookIndex;

    /**
     * The number of the chapter of the verse within its book.
     */
    private int chapterNum;

    /**
     * The number of the verse within its chapter.
     */
    private int verseNum;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Passes up the request path.
     */
    public GetVerseIndexByReferenceRequest() {
        super(REQUEST_PATH);
    }

    /* ---------- METHODS ---------- */

    /**
     * Get the index of the book of the verse
     * within the Bible.
     *
     * @return the book index
     */
    public int getBookIndex() {
        return bookIndex;
    }

    /**
     * Get the number of the chapter of the verse
     * within its book.
     *
     * @return the chapter number
     */
    public int getChapterNum() {
        return chapterNum;
    }

    /**
     * Get the number of the verse within its chapter.
     *
     * @return the verse number
     */
    public int getVerseNum() {
        return verseNum;
    }

    /**
     * Attempts to retrieve and parse the reference
     * of the verse from the parameters.
     *
     * @param parameters the map of parameters
     * @return true on success, false otherwise
     */
    @Override
    public boolean parse(Map<String, String> parameters) {

        // Attempt to pull all parameters from the map.
        Object[] referenceFields = new Object[]{
                parameters.get("bookIndex"),
                parameters.get("chapterNum"),
                parameters.get("verseNum")
        };

        // Check for validity
        for (int i = 0; i < referenceFields.length; i++) {

            if (referenceFields[i] == null) {
                // Field not in map
                return false;
            }

            String fieldAsString = (String) referenceFields[i];

            if (fieldAsString.isBlank()) {
                // Field has no content
                return false;
            }

            try {
                int fieldAsInt = Integer.parseInt(fieldAsString);
                referenceFields[i] = fieldAsInt;
            } catch (NumberFormatException e) {
                // Field is not an integer
                return false;
            }

        }

        // Success! Set instance variables.
        this.bookIndex = (int) referenceFields[0];
        this.chapterNum = (int) referenceFields[1];
        this.verseNum = (int) referenceFields[2];
        return true;

    }

}
