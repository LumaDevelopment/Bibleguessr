package gg.bibleguessr.bible.requests;

import gg.bibleguessr.bible.Bible;
import gg.bibleguessr.service_wrapper.Request;

import java.util.Map;

/**
 * Request object that represents requests made
 * by the frontend for a random Bible verse
 * (and surrounding context verses).
 */
public class RandomVerseRequest extends Request {

    /* ---------- CONSTANTS ---------- */

    /**
     * The path of this request.
     */
    public static final String REQUEST_PATH = "random-verse";

    /* ---------- VARIABLES ---------- */

    /**
     * The version to pull the random verse from.
     */
    private String version;

    /**
     * How many surrounding verses should be retrieved
     * to serve as context for the random verse.
     */
    private int numOfContextVerses;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Passes up the request path.
     */
    public RandomVerseRequest() {
        super(REQUEST_PATH);
    }

    /* ---------- METHODS ---------- */

    /**
     * Get the number of verses that should be
     * provided as context with the random verse.
     *
     * @return the number of context verses
     */
    public int getNumOfContextVerses() {
        return numOfContextVerses;
    }

    /**
     * Get the version to pull the random verse from.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Attempts to retrieve and parse the version
     * and number of context verses from the
     * parameters.
     *
     * @param parameters the map of parameters
     * @return true if successful, false otherwise
     */
    @Override
    public boolean parse(Map<String, String> parameters) {

        try {

            // Attempt to pull the version from the parameters
            String version = parameters.get("version");

            if (version == null || version.isBlank()) {
                return false;
            }

            // Attempt to pull the number of context verses,
            // as text, from the parameters
            String numOfContextVersesText = parameters.get("numOfContextVerses");

            if (numOfContextVersesText == null) {
                return false;
            }

            // Attempt to parse the number of context verses
            // as its correct type
            int numOfContextVerses = Integer.parseInt(numOfContextVersesText);

            // Make sure the number of context verses is within reasonable bounds
            if (numOfContextVerses < 0 || numOfContextVerses > Bible.MAX_CONTEXT_VERSES) {
                return false;
            }

            // Success!
            this.version = version;
            this.numOfContextVerses = numOfContextVerses;
            return true;

        } catch (Exception e) {
            // If any error occurs, the parse is not successful
            return false;
        }

    }

}
