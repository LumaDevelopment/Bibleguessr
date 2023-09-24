package gg.bibleguessr.bible;

import gg.bibleguessr.bible.objs.Chapter;
import gg.bibleguessr.bible.objs.Verse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages retrieving passages from Bible text files.<br>
 * This class can either read Bible text into memory (high speed,
 * high memory) or store offsets to the start of each chapter in
 * the Bible text files (low speed, low memory).<br>
 * This class can be instantiated for use with the main Bible service
 * by passing in BibleService to the constructor, or for testing and
 * benchmarking by passing in storeBibleTextInMemory, bibleFiles,
 * and bibleFileExtension.
 */
public class BibleReadingMgr {

    /* ---------- CONSTANTS ---------- */

    /**
     * Name used for the logger used by this class.
     * The logger name is the name of the class.
     */
    public static final String LOGGER_NAME = BibleReadingMgr.class.getSimpleName();

    /* ---------- VARIABLES ---------- */

    /**
     * Logger used by this class.
     */
    private final Logger logger;

    /**
     * Whether to store bible text in memory or
     * store chapter offsets.
     */
    private boolean storeBibleTextInMemory;

    /**
     * The total length of the file extension
     * (including the ".") of the Bible files.
     */
    private int totalExtensionLength;

    /**
     * The Bible singleton. Used to get chapters
     * and verses by reference.
     */
    private final Bible bible;

    /**
     * Maintains a map of version name to the
     * RandomAccessFile of the version's file.
     * If <code>storeBibleTextInMemory</code>
     * is <code>true</code>, all files are closed
     * and the map is destroyed after initialization.
     */
    private Map<String, RandomAccessFile> versionFiles;

    /**
     * Maintains a map of version name to a map of
     * chapter to the location of the chapter's
     * start in the version file. Only used if
     * <code>storeBibleTextInMemory</code> is false.
     */
    private Map<String, Map<Chapter, Long>> versionChapterOffsets;

    /**
     * Maintains a map of version name to a map of
     * verse to the verse's text. Only used if
     * <code>storeBibleTextInMemory</code> is true.
     */
    private Map<String, Map<Verse, String>> versionVerseTexts;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Private constructor called by the two other
     * constructors of the class that sets variables
     * that don't have different values depending on
     * the constructor.
     */
    private BibleReadingMgr() {

        this.logger = LoggerFactory.getLogger(LOGGER_NAME);

        this.bible = Bible.getInstance();
        this.versionFiles = null;
        this.versionChapterOffsets = null;
        this.versionVerseTexts = null;

    }

    /**
     * Constructor for usage of BibleReadingMgr in the
     * main Bible microservice.
     *
     * @param bibleService The BibleService object to use
     */
    public BibleReadingMgr(BibleService bibleService) {

        this();

        BibleServiceConfig config = bibleService.getConfig();

        this.storeBibleTextInMemory = config.storeBibleTextInMemory();
        // 1 represents the length of the "." for the extension start
        this.totalExtensionLength = 1 + config.bibleFileExtension().length();

        this.versionFiles = openRandomAccessFiles(bibleService.getBibleFiles());

        finishInitialization();

    }

    /**
     * Constructor for usage of BibleReadingMgr in testing
     * and benchmarking.
     *
     * @param storeBibleTextInMemory Whether to store Bible text in memory or not
     * @param bibleFiles             The Bible files to use
     * @param bibleFileExtension     The file extension of the Bible files
     */
    public BibleReadingMgr(boolean storeBibleTextInMemory, File[] bibleFiles, String bibleFileExtension) {

        this();

        this.storeBibleTextInMemory = storeBibleTextInMemory;
        // 1 represents the length of the "." for the extension start
        this.totalExtensionLength = 1 + bibleFileExtension.length();

        this.versionFiles = openRandomAccessFiles(bibleFiles);

        finishInitialization();

    }

    /* ---------- METHODS ---------- */

    /**
     * Using the given map of version names to
     * version RandomAccessFiles, calculates
     * the starting position of each chapter of
     * the Bible in the version file for every
     * version.
     *
     * @param versionFiles Map of version names to version RandomAccessFiles
     * @return Map of version names to map of chapter to chapter position in version file
     */
    public Map<String, Map<Chapter, Long>> calculateVersionChapterOffsets(Map<String, RandomAccessFile> versionFiles) {

        Map<String, Map<Chapter, Long>> versionChapterOffsets = new HashMap<>();

        // Loop through every version
        for (Map.Entry<String, RandomAccessFile> versionEntry : versionFiles.entrySet()) {

            try {

                // Time calculation
                long offsetCalculationStart = System.currentTimeMillis();

                // Store the chapter offsets for this version
                // using this version's RandomAccessFile
                Map<Chapter, Long> chapterOffsets = new HashMap<>();
                RandomAccessFile versionFile = versionEntry.getValue();

                // Read each line and keep track of last position
                // so when we read a line with a new chapter,
                // we know where the chapter starts.
                String line = versionFile.readLine();
                long lastPos = 0;

                while (line != null) {

                    // Pass in full line to function, still works.
                    Chapter chapter = bible.getChapterByReference(line);

                    if (chapter == null) {
                        // Shouldn't happen.
                        logger.error("Version file contains an invalid chapter reference: " +
                                "\"" + line.substring(0, 11) + "\"");
                        return null;
                    }

                    if (!chapterOffsets.containsKey(chapter)) {
                        // If this is a new chapter, store its
                        // start position (which is the ending
                        // position of the last line)
                        chapterOffsets.put(chapter, lastPos);
                    }

                    // Update variables and continue loop
                    lastPos = versionFile.getFilePointer();
                    line = versionFile.readLine();

                }

                // Add this version's chapter offsets to the map
                // of all version chapter offsets
                versionChapterOffsets.put(versionEntry.getKey(), chapterOffsets);

                // Time calculation
                long offsetCalculationEnd = System.currentTimeMillis();

                logger.info("\"{}\" offsets have been calculated in {} s.",
                        versionEntry.getKey(), (offsetCalculationEnd - offsetCalculationStart) / 1000.0);

            } catch (IOException ex) {
                // Per version basis so that we can keep going
                // if one or more versions has an invalid file.
                logger.error("Encountered an IOException while attempting to read from the file for " +
                        "version \"" + versionEntry.getKey() + "\".", ex);
            }

        }

        return versionChapterOffsets;

    }

    /**
     * Closes the random access files used by this class.
     * If we are storing text in memory, this is done after
     * class initialization. If not, this is done when
     * Bible service shuts down (or when benchmarking/testing
     * is complete).
     */
    public void closeRandomAccessFiles() {

        if (versionFiles == null) {
            // This method has already run.
            return;
        }

        // Close all open RandomAccessFiles
        for (RandomAccessFile versionFile : versionFiles.values()) {
            try {
                versionFile.close();
            } catch (IOException ex) {
                // Ignore, we're shutting down.
            }
        }

        // Set to null, so we know this method has already run,
        // also to free up memory.
        versionFiles = null;

    }

    /**
     * Finishes the initialization started by either of the
     * constructors. This method should only be called once.
     * This method initializes either the
     * <code>versionVerseTexts</code> or
     * <code>versionChapterOffsets</code> map, depending on
     * if <code>storeBibleTextInMemory</code> is true or
     * false, respectively. If the map is null, a
     * RuntimeException is thrown.
     */
    private void finishInitialization() {

        boolean mapIsNull;

        if (storeBibleTextInMemory) {

            // If we're storing all bible text in memory,
            // attempt to do that, then close all
            // random access files and record if we read
            // all the text into memory successfully.

            this.versionVerseTexts = readVerseTextIntoMemory(this.versionFiles);
            mapIsNull = versionVerseTexts == null;
            closeRandomAccessFiles();

        } else {

            // If we're storing chapter offsets, calculate
            // them and record if we calculated them
            // successfully.

            this.versionChapterOffsets = calculateVersionChapterOffsets(this.versionFiles);
            mapIsNull = versionChapterOffsets == null;

        }

        if (mapIsNull) {
            // If the map is null, the microservice cannot function.
            throw new RuntimeException(BibleReadingMgr.class.getSimpleName() + " could not be initialized " +
                    "because of invalid Bible files, logs contain which file is invalid.");
        }

        logger.info(BibleReadingMgr.class.getSimpleName() + " is done initializing! Storing Bible text in " +
                (storeBibleTextInMemory ? "memory." : "files."));

    }

    /**
     * Get the text for the passage defined by the
     * start and end verses (inclusive) from the
     * given version of the Bible. Start and end
     * verse do not wrap (i.e. no Revelation ->
     * Genesis type passages).
     *
     * @param version    The version of the Bible to get the text from (case-insensitive)
     * @param startVerse The start verse of the passage
     * @param endVerse   The end verse of the passage
     * @return The text of the passage, or null if: any of the parameters
     * are null, the version does not exist, or the passage bounds are invalid.
     */
    public String getPassageText(String version, Verse startVerse, Verse endVerse) {

        // Make sure all parameters exist
        if (version == null || startVerse == null || endVerse == null) {
            logger.error("Attempted to call getPassageText() with one ore more null parameters!");
            return null;
        }

        // Standardization
        version = version.toLowerCase();

        // Make sure the end verse doesn't come
        // before the start verse
        if (endVerse.compareTo(startVerse) < 0) {
            logger.warn("Attempted to obtain passage text with end verse coming before start verse.");
            return null;
        }

        // Since we could be doing a large deal of
        // concatenation, use a StringBuilder
        // for passage text until the point of return.
        StringBuilder passageText = new StringBuilder();

        // Method behavior changes wildly depending on
        // if we're storing Bible text in memory or not.
        if (storeBibleTextInMemory) {

            // Check if the version even exists
            if (!versionVerseTexts.containsKey(version)) {
                logger.warn("Attempted to obtain passage text from version which does not exist: \"{}\"", version);
                return null;
            }

            Verse currentVerse = startVerse;

            // Loop through all verses in the passage
            while (currentVerse != null) {

                // Add the passage text retrieved from the map, and append
                // a space to separate from the next verse.
                passageText.append(versionVerseTexts.get(version).get(currentVerse)).append(" ");

                if (currentVerse.equals(endVerse)) {
                    // If this is the last verse, set currentVerse to null
                    // so the loop runs no more.
                    currentVerse = null;
                } else {
                    // Hop to the next verse in the Bible.
                    currentVerse = currentVerse.nextVerse();
                }

            }

            // Return the assembled text, removing the extra space at the end.
            return passageText.deleteCharAt(passageText.length() - 1).toString();

        } else {

            // Check if the version even exists
            if (!versionChapterOffsets.containsKey(version)) {
                logger.warn("Attempted to obtain passage text from version which does not exist: \"{}\"", version);
                return null;
            }

            // Wrap in try-catch because of IO operations
            try {

                // Get the file associated with the version
                RandomAccessFile versionFile = versionFiles.get(version);

                // Jump to the chapter of the start verse
                versionFile.seek(versionChapterOffsets.get(version).get(startVerse.chapter()));

                // Read until we hit the first verse of the passage
                String currentLine = versionFile.readLine();

                while (currentLine != null && !currentLine.startsWith(startVerse.reference())) {
                    currentLine = versionFile.readLine();
                }

                if (currentLine == null) {
                    // For some reason, the version file ends
                    // before we got all the verses, or an IO
                    // issue occurs, or the verse isn't in
                    // the file. Shouldn't happen
                    logger.error("Couldn't find passage in version file. Version: {}. Start verse: {}. End verse: {}.",
                            version, startVerse, endVerse);
                    return null;
                }

                // Just a quick check if start verse
                // and end verse are the same
                if (startVerse.equals(endVerse)) {
                    // If so, return the line for efficiency
                    return currentLine.substring(12);
                }

                // Keep reading until we've read all the
                // verses in the passage
                while (currentLine != null) {

                    // Add the passage text retrieved from the map, and append
                    // a space to separate from the next verse.
                    passageText.append(currentLine.substring(12)).append(" ");

                    if (currentLine.startsWith(endVerse.reference())) {
                        // If this is the last verse, set currentLine to null
                        // so the loop runs no more.
                        currentLine = null;
                    } else {
                        // Hop to the next verse in the Bible.
                        currentLine = versionFile.readLine();
                    }

                }

                // Return the assembled text, removing the extra space at the end.
                return passageText.deleteCharAt(passageText.length() - 1).toString();

            } catch (IOException ex) {
                logger.error("Encountered an IOException while attempting to read passage text from " +
                        "\"" + version + "\".", ex);
                return null;
            }

        }

    }

    /**
     * Gets the text of the given verse from
     * the given version of the Bible. Calls
     * getPassageText() with the same start
     * and end verse.
     *
     * @param version The version of the Bible to get the text from (case-insensitive)
     * @param verse   The verse to get the text of
     * @return The text of the verse, or null if an error occurs
     * (see getPassageText() doc).
     */
    public String getVerseText(String version, Verse verse) {
        return getPassageText(version, verse, verse);
    }

    /**
     * Gets the version name from the name of
     * the version's file. Removes the file
     * extension and converts to lowercase.
     *
     * @param filename The name of the version's file
     * @return The version name
     */
    public String getVersionNameFromFilename(String filename) {
        return filename.substring(0, filename.length() - totalExtensionLength).toLowerCase();
    }

    /**
     * Opens a RandomAccessFile for each Bible file
     * given. The key for each entry in the map is
     * the version name.
     *
     * @param bibleFiles The Bible files to open
     * @return Map of version name to version RandomAccessFile
     */
    public Map<String, RandomAccessFile> openRandomAccessFiles(File[] bibleFiles) {

        Map<String, RandomAccessFile> versionRAFs = new HashMap<>();

        for (File bibleFile : bibleFiles) {
            try {
                versionRAFs.put(
                        getVersionNameFromFilename(bibleFile.getName()),
                        new RandomAccessFile(bibleFile, "r")
                );
            } catch (FileNotFoundException ex) {
                logger.error("Could not find Bible file: " + bibleFile.getAbsolutePath());
            }
        }

        return versionRAFs;

    }

    /**
     * Reads each verse from each version file into
     * a map from version to map of verse to verse text.
     *
     * @param versionFiles Map of version name to version RandomAccessFile
     * @return Map of version name to map of verse to verse text
     */
    public Map<String, Map<Verse, String>> readVerseTextIntoMemory(Map<String, RandomAccessFile> versionFiles) {

        Map<String, Map<Verse, String>> versionVerseTexts = new HashMap<>();

        // Loop through every version
        for (Map.Entry<String, RandomAccessFile> versionEntry : versionFiles.entrySet()) {

            try {

                // Time calculation
                long verseTextReadStart = System.currentTimeMillis();

                // Collect verse text for every verse in the
                // version's file
                Map<Verse, String> verseTexts = new HashMap<>();
                RandomAccessFile versionFile = versionEntry.getValue();

                // Read every line of the file in a loop.
                String line = versionFile.readLine();

                while (line != null) {

                    // Pass in full line to function, still works.
                    Verse verse = bible.getVerseByReference(line);

                    if (verse == null) {
                        // Shouldn't happen.
                        logger.error("Version file contains an invalid verse reference: " +
                                "\"" + line.substring(0, 11) + "\"");
                        return null;
                    }

                    // Add the verse text to the map with the
                    // reference cut out.
                    verseTexts.put(verse, line.substring(12));

                    // Update variable and continue loop
                    line = versionFile.readLine();

                }

                // Add this version's verse texts to the map
                versionVerseTexts.put(versionEntry.getKey(), verseTexts);

                // Time calculation
                long verseTextReadEnd = System.currentTimeMillis();

                logger.info("\"{}\" texts have been read into memory in {} s.",
                        versionEntry.getKey(), (verseTextReadEnd - verseTextReadStart) / 1000.0);

            } catch (IOException ex) {
                logger.error("Encountered an IOException while attempting to read from the file for " +
                        "version \"" + versionEntry.getKey() + "\".", ex);
            }

        }

        return versionVerseTexts;

    }


}
