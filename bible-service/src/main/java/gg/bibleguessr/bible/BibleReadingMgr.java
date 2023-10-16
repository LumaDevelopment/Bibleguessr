package gg.bibleguessr.bible;

import gg.bibleguessr.bible.objs.Book;
import gg.bibleguessr.bible.objs.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BibleReadingMgr {

    /* ---------- CONSTANTS ---------- */

    /**
     * Identifies this class's logger.
     */
    public static final String LOGGER_NAME = BibleReadingMgr.class.getSimpleName();

    /* ---------- VARIABLES ---------- */

    /**
     * The logger used by this class.
     */
    private final Logger logger;

    /**
     * Regex that pulls the chapter:verse part of
     * a verse reference out for removal.
     */
    private final Pattern chapterVersePattern;

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
     * @param bibleFileExtension The extension of all Bible version files.
     * @param bibleFiles         The Bible version files.
     */
    public BibleReadingMgr(String bibleFileExtension, File[] bibleFiles) {

        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.chapterVersePattern = Pattern.compile("\\b\\d+:\\d+\\b");
        this.bibleFileExtensionLength = bibleFileExtension.length() + 1;
        this.bibleFiles = bibleFiles;
        this.bibleText = new HashMap<>();

        initializeText();

    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Given a Bible version file, attempts to add it
     * to the Bible text map.
     *
     * @param versionFile The Bible version file to add.
     * @return <code>true</code> if the version was added
     * successfully, <code>false</code> otherwise.
     */
    public boolean addVersion(File versionFile) {

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
        String[] verseText = new String[BibleService.VERSES_IN_BIBLE];

        try (BufferedReader br = new BufferedReader(new FileReader(versionFile))) {

            String line;
            while ((line = br.readLine()) != null) {

                // Make sure no trailing characters on the line
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // The split between verse reference and
                // verse text is the pipe character.
                String[] lineParts = line.split("\\|");

                if (lineParts.length < 1 || lineParts.length > 2) {
                    logger.error("Bible file (" + versionFile.getName() + ") with " +
                            "invalid formatting! Offending line: " + line);
                    return false;
                }

                // The first part of the line is structured like
                // "Genesis 1:1", so get the book name out of this
                // by finding the chapter:verse portion with regex
                // and using substring to cut it out
                String bookName = getBookNameFromReference(lineParts[0]);

                if (bookName == null) {
                    logger.error("Bible file (" + versionFile.getName() + ") with " +
                            "invalid formatting! Offending line: " + line);
                    return false;
                }

                // Update book name if it is different
                if (!bookName.equals(currentBookName)) {

                    if (currentBookIndex != -1) {
                        // Put previous book name into map
                        bookNames.put(
                                Bible.getInstance().getBookByIndex(currentBookIndex),
                                currentBookName
                        );
                    }

                    // Set new index and name
                    currentBookIndex++;
                    currentBookName = bookName;

                }

                // Insert the verse's text into the array
                if (lineParts.length < 2) {
                    verseText[verseIndex] = "";
                } else {
                    verseText[verseIndex] = lineParts[1];
                }

                verseIndex++;

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
    public Map<String, Version> getBibleVersions() {

        HashMap<String, Version> versionsMap = new HashMap<>();

        for (Version version : bibleText.keySet()) {
            versionsMap.put(version.getName(), version);
        }

        return versionsMap;

    }

    /**
     * Given a reference to a Bible verse (i.e. Genesis 1:1),
     * extracts the name of the book from it. Compatible with
     * books that have numbers and spaces in their names.
     *
     * @param reference The reference to the verse.
     * @return The name of the book, or <code>null</code> if
     * the reference is invalid.
     */
    public String getBookNameFromReference(String reference) {

        if (reference == null) {
            return null;
        }

        Matcher matcher = chapterVersePattern.matcher(reference);
        String chapterVerseReference;

        if (matcher.find()) {
            chapterVerseReference = matcher.group();
        } else {
            return null;
        }

        int charactersToCut = chapterVerseReference.length() + 1;
        return reference.substring(0, reference.length() - charactersToCut);

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
                startUniversalIndex >= BibleService.VERSES_IN_BIBLE ||
                endUniversalIndex < 0 ||
                endUniversalIndex >= BibleService.VERSES_IN_BIBLE) {
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
                universalIndex >= BibleService.VERSES_IN_BIBLE) {
            return null;
        }

        return bibleText.get(version)[universalIndex];

    }

    /**
     * Reads through all versions of the Bible. Creates a
     * Version object for every version of the Bible.
     * Extracts the verse text and book names from every
     * version of the Bible. Stores all extracted information
     * for easy retrieval.
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

            if (addVersion(versionFile)) {
                successes++;
            }

            total++;

        }

        logger.info("Finished reading Bible versions, {}/{} were successful.", successes, total);

    }

}
