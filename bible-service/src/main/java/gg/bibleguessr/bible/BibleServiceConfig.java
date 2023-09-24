package gg.bibleguessr.bible;

/**
 * The configuration object for the Bible service.
 *
 * @param biblesDirectory        The directory where the Bible files are stored.
 * @param bibleFileExtension     The file extension of valid Bible files. Doesn't include "."
 * @param storeBibleTextInMemory Determines whether to read all Bible text in memory,
 *                               or instead calculate chapter locations in files and read
 *                               from them when needed. Read as needed reduces text retrieval
 *                               memory usage by 28% on average, however, text retrieval speed
 *                               slows by 2x in best case, 20x in worst case. Startup time is
 *                               roughly equivalent.
 */
public record BibleServiceConfig(
        String biblesDirectory,
        String bibleFileExtension,
        boolean storeBibleTextInMemory
) {

    /**
     * Gets the default configuration for the Bible service:<br>
     * - {@code biblesDirectory} is {@code "bibles"}<br>
     * - {@code bibleFileExtension} is {@code "txt"}<br>
     * - {@code storeBibleTextInMemory} is {@code true}
     *
     * @return The default configuration for the Bible service
     */
    @SuppressWarnings("unused")
    public static BibleServiceConfig getDefault() {
        return new BibleServiceConfig(
                "bibles",
                "txt",
                true
        );
    }

}
