package gg.bibleguessr.bible;

import gg.bibleguessr.bible.objs.Verse;

import java.io.File;

/**
 * Class containing some benchmarks that can be run to evaluate
 * the performance of the Bible microservice.
 */
public class Benchmarks {

    /**
     * Benchmarks how long it takes to retrieve the
     * text of the passage with the given start
     * and end verse from the Bible text file of
     * the given version. Returns an array of the
     * following metrics:<br>
     * - How long it took to initialize the BibleReadingMgr (in ms)<br>
     * - How long it took to retrieve the passage text (in ms)<br>
     * - How much memory was in use directly after retrieval (in bytes)
     *
     * @param storeBibleTextInMemory Whether to store the Bible text in memory
     * @param bibleFiles             The Bible files to read from
     * @param bibleFileExtension     The file extension of the Bible files (not including ".")
     * @param version                The version of the Bible to read from
     * @param startVerse             The start verse of the passage
     * @param endVerse               The end verse of the passage
     * @return An array of the metrics described above
     */
    public static Long[] getPassageText(boolean storeBibleTextInMemory, File[] bibleFiles, String bibleFileExtension,
                                        String version, Verse startVerse, Verse endVerse) {

        // Measure how long it takes to initialize the BibleReadingMgr
        long initReadingMgrStartTime = System.currentTimeMillis();
        BibleReadingMgr bibleReadingMgr = new BibleReadingMgr(storeBibleTextInMemory, bibleFiles, bibleFileExtension);
        long initReadingMgrEndTime = System.currentTimeMillis();

        // Measure how long it takes to retrieve the passage text
        long retrievePassageStartTime = System.currentTimeMillis();
        bibleReadingMgr.getPassageText(version, startVerse, endVerse);
        long retrievePassageEndTime = System.currentTimeMillis();

        // Measure memory usage at the point of retrieval
        long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Calculate efficiency metrics
        long initReadingMgrTimeInMs = initReadingMgrEndTime - initReadingMgrStartTime;
        long retrievePassageTimeInMs = retrievePassageEndTime - retrievePassageStartTime;

        // Assemble metrics, shut down the reading manager, and return
        Long[] metrics = {initReadingMgrTimeInMs, retrievePassageTimeInMs, memoryUsed};
        bibleReadingMgr.closeRandomAccessFiles();
        return metrics;

    }

}
