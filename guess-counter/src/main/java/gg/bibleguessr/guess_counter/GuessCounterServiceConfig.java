package gg.bibleguessr.guess_counter;

/**
 * The configuration object for the Guess Counter service.
 *
 * @param counterFileName   The name of the file where we write the number of guesses made,
 *                          so that it is persistent between service restarts.
 * @param writeIntervalInMs The interval in milliseconds between writes to the counter
 *                          file.
 */
public record GuessCounterServiceConfig(
        String counterFileName,
        long writeIntervalInMs
) {

    /**
     * Gets the default configuration for the Guess Counter service:<br>
     * - {@code counterFileName} is {@code "guess_counter.txt"}<br>
     * - {@code writeIntervalInMs} is {@code 1_000}<br>
     *
     * @return The default configuration for the Guess Counter service
     */
    public static GuessCounterServiceConfig getDefault() {
        return new GuessCounterServiceConfig(
                "guess_counter.dat",
                1_000
        );
    }

}
