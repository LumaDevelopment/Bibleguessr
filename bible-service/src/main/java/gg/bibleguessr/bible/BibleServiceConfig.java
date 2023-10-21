package gg.bibleguessr.bible;

/**
 * The configuration object for the Bible service.
 *
 * @param biblesDirectory    The directory where the Bible files are stored.
 * @param bibleFileExtension The file extension of valid Bible files. Doesn't include "."
 */
public record BibleServiceConfig(
        String biblesDirectory,
        String bibleFileExtension
) {

    /**
     * Gets the default configuration for the Bible service:<br>
     * - {@code biblesDirectory} is {@code "bibles"}<br>
     * - {@code bibleFileExtension} is {@code "txt"}<br>
     *
     * @return The default configuration for the Bible service
     */
    @SuppressWarnings("unused")
    public static BibleServiceConfig getDefault() {
        return new BibleServiceConfig(
                "bibles",
                "txt"
        );
    }

}
