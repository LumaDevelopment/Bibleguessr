package gg.bibleguessr.bible;

import gg.bibleguessr.backend_utils.BibleguessrUtilities;
import gg.bibleguessr.bible.data_structures.Book;
import gg.bibleguessr.bible.data_structures.Version;
import gg.bibleguessr.bible.versions.BibleVersionMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages reading the Bible text
 * from version files. Stores the text in
 * memory for easy retrieval. Notifies
 * the BibleVersionMgr when the list of
 * versions changes.
 */
public class BibleTextMgr {

    /* ---------- CONSTANTS ---------- */

    /**
     * Identifies this class's logger.
     */
    public static final String LOGGER_NAME = BibleTextMgr.class.getSimpleName();

    /* ---------- VARIABLES ---------- */

    /**
     * The logger used by this class.
     */
    private final Logger logger;

    /**
     * The class to notify whenever the list
     * of available Bible versions is detected
     * to have changed by this class.
     */
    private final BibleVersionMgr bibleVersionMgr;

    /**
     * The length of the extension of all Bible text files.
     */
    private final int bibleFileExtensionLength;

    /**
     * All the Bible version files.
     */
    private final File[] bibleFiles;

    /**
     * All Bible text, stored as a map from Version
     * to an array of all verses.
     */
    private final Map<Version, String[]> bibleText;

    /* ---------- CONSTRUCTOR ---------- */

    /**
     * Creates a new instance of BibleReadingMgr.
     *
     * @param bibleVersionMgr    The Bible version manager.
     * @param bibleFileExtension The extension of all Bible version files.
     * @param bibleFiles         The Bible version files.
     */
    public BibleTextMgr(BibleVersionMgr bibleVersionMgr,
                        String bibleFileExtension, File[] bibleFiles) {

        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.bibleVersionMgr = bibleVersionMgr;
        this.bibleFileExtensionLength = bibleFileExtension.length() + 1;
        this.bibleFiles = bibleFiles;
        this.bibleText = new HashMap<>();

        initializeText();

    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Attempts to add a new version from the given
     * Bible version file. If it is successful,
     * the change will be reflected in BibleVersionMgr
     * and all classes that use Bible versions
     * will be notified.
     *
     * @param versionFile The Bible version file to add.
     * @return <code>true</code> if the version was added
     * successfully, <code>false</code> otherwise.
     */
    public boolean addVersion(File versionFile) {
        return addVersion(versionFile, true);
    }

    /**
     * Given a Bible version file, attempts to add it
     * to the Bible text map.
     *
     * @param versionFile      The Bible version file to add.
     * @param notifyVersionMgr Whether to notify the
     *                         BibleVersionMgr that a new
     *                         version was added.
     * @return <code>true</code> if the version was added
     * successfully, <code>false</code> otherwise.
     */
    private boolean addVersion(File versionFile, boolean notifyVersionMgr) {

        // Start recording time to add
        long startTime = System.currentTimeMillis();

        // Cut out the ".txt" (or whatever the file extension is
        // from the file name to get the version name
        String versionName = versionFile.getName().substring(0,
                versionFile.getName().length() - bibleFileExtensionLength);

        // Extract the names of every book of the bible
        // from the bible text
        int currentBookIndex = -1;
        String currentBookName = "";
        Map<Book, String> bookNames = new HashMap<>();

        // Store the text of all verses in the Bible
        int verseIndex = 0;
        String[] verseText = new String[Bible.NUM_OF_VERSES];

        try (BufferedReader br = new BufferedReader(new FileReader(versionFile))) {

            String line;
            while ((line = br.readLine()) != null) {

                // Doing any trimming or skip empty operations
                // would skip over intentional empty lines,
                // so don't do that

                if (!line.isEmpty() && line.charAt(0) == '|') {

                    // This is a book name

                    if (currentBookIndex != -1) {
                        // Put previous book name into map
                        bookNames.put(
                                Bible.getInstance().getBookByIndex(currentBookIndex),
                                currentBookName
                        );
                    }

                    // Set new index and name
                    currentBookIndex++;
                    currentBookName = line.substring(1);

                } else {

                    // This is verse text
                    verseText[verseIndex] = line;
                    verseIndex++;

                }

            }

            // Add final book name
            bookNames.put(
                    Bible.getInstance().getBookByIndex(currentBookIndex),
                    currentBookName
            );

            // Create Version object
            Version version = new Version(
                    versionName,
                    bookNames
            );

            // Store version object with Bible text
            bibleText.put(version, verseText);

            // Stop recording time to add
            long endTime = System.currentTimeMillis();

            if (notifyVersionMgr) {
                // Notify the BibleVersionMgr that
                // a single version has been added.
                // This will add the version to the
                // frontend bible data.
                bibleVersionMgr.addAvailableVersion(version);
            }

            // Calculate statistics
            long timeToAddInMs = endTime - startTime;
            double timeToAddInS = timeToAddInMs / 1_000.0;
            int versesPerSecond = (int) (Bible.NUM_OF_VERSES / timeToAddInS);

            long fileSizeInBytes = versionFile.length();
            double fileSizeInMBs = fileSizeInBytes / 1_000_000.0;

            // Print statistics
            String format =  "Successfully added Bible version: \"%s\". %.2f MB file loaded in %d milliseconds. %d " +
                    "verses stored per second.";
            logger.info(String.format(format, versionName, fileSizeInMBs, timeToAddInMs, versesPerSecond));

            return true;

        } catch (IOException e) {
            logger.error("Cannot read bible version: " + versionName, e);
            return false;
        }

    }

    /**
     * Gets a Map of all Bible versions that
     * this class has read.
     *
     * @return A Map of all Bible versions that
     * this class has read.
     */
    private Map<String, Version> composeBibleVersionsMap() {

        HashMap<String, Version> versionsMap = new HashMap<>();

        for (Version version : bibleText.keySet()) {
            versionsMap.put(version.getName(), version);
        }

        return versionsMap;

    }

    /**
     * Attempts to get the text of the passage of the
     * Bible with the given universal indices, from
     * the given version of the Bible.
     *
     * @param version             The Version of the Bible to read from.
     * @param startUniversalIndex The universal index of the first
     *                            verse to read (inclusive).
     * @param endUniversalIndex   The universal index of the last
     *                            verse to read (inclusive).
     * @return The text of the passage, or <code>null</code> if the parameters
     * are invalid. Can also return <code>null</code> if some other internal
     * error occurs.
     */
    public String getPassageText(Version version, int startUniversalIndex, int endUniversalIndex) {

        if (version == null ||
                !bibleText.containsKey(version) ||
                startUniversalIndex < 0 ||
                startUniversalIndex >= Bible.NUM_OF_VERSES ||
                endUniversalIndex < 0 ||
                endUniversalIndex >= Bible.NUM_OF_VERSES) {
            return null;
        }

        StringBuilder passageText = new StringBuilder();

        for (int i = startUniversalIndex; i <= endUniversalIndex; i++) {
            passageText.append(bibleText.get(version)[i]);
            passageText.append(" ");
        }

        return passageText.toString().trim();

    }

    /**
     * Attempts to get the text of the verse of the
     * Bible with the given universal index, from
     * the given version of the Bible.
     *
     * @param version        The version of the Bible to read the verse from.
     * @param universalIndex The universal index of the verse to read.
     * @return The text of the verse, or <code>null</code> if the parameters
     * are invalid. Can also return <code>null</code> if some other internal
     * error occurs.
     */
    public String getVerseText(Version version, int universalIndex) {

        if (version == null ||
                !bibleText.containsKey(version) ||
                universalIndex < 0 ||
                universalIndex >= Bible.NUM_OF_VERSES) {
            return null;
        }

        return bibleText.get(version)[universalIndex];

    }

    /**
     * Reads through all versions of the Bible. Creates a
     * Version object for every version of the Bible.
     * Extracts the verse text and book names from every
     * version of the Bible. Stores all extracted information
     * for easy retrieval. Can be run multiple times
     * if you want to react to new versions being added
     * on the fly.
     */
    public void initializeText() {

        if (!bibleText.isEmpty()) {
            // Re-initialization
            bibleText.clear();
        }

        int successes = 0;
        int total = 0;

        // Loop through all version files
        for (File versionFile : bibleFiles) {

            if (addVersion(versionFile, false)) {
                successes++;
            }

            total++;

        }

        // Clear unused memory and get how much memory in use.
        System.gc();
        long memoryInUseInBytes = BibleguessrUtilities.getMemoryInUse();
        double memoryInUseInMBs = memoryInUseInBytes / 1_000_000.0;

        logger.info("Finished reading Bible versions, {}/{} were successful. {} MB of memory in use.",
                successes, total, memoryInUseInMBs);

        // Since the text map is being fully reset,
        // just tell the BibleVersionMgr that the
        // whole thing has changed.
        bibleVersionMgr.setAvailableVersions(composeBibleVersionsMap());

    }

}
