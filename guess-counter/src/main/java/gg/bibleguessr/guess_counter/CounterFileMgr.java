package gg.bibleguessr.guess_counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * Manages the counter file, which stores the number of guesses
 * that have been made. This class ensures it is thread-safe.
 */
public class CounterFileMgr {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of the logger for this class.
     */
    public static final String LOGGER_NAME = CounterFileMgr.class.getSimpleName();

    /* ---------- VARIABLES ---------- */

    /**
     * The logger for this class.
     */
    private final Logger logger;

    /**
     * The counter file. This is the file that stores the number of guesses
     * that have been made.
     */
    private final File counterFile;

    /**
     * RandomAccessFile, used for quick count updates
     * and count reading.
     */
    private final RandomAccessFile raf;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Creates a new counter file manager.
     *
     * @param counterFileName The name of the counter file.
     */
    public CounterFileMgr(String counterFileName) {

        // Core variables
        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.counterFile = new File(counterFileName);

        // If counter file doesn't already exist,
        // create it
        boolean didCounterFileExist = true;

        if (!this.counterFile.exists()) {
            createCounterFile();
            didCounterFileExist = false;
        }

        // Open a RandomAccessFile on this file
        // that updates the file on the disk every
        // time we make a change
        try {
            this.raf = new RandomAccessFile(counterFile, "rwd");
        } catch (Exception e) {
            logger.error("Exception raised while trying to open a RandomAccessFile for the counter file.", e);
            throw new RuntimeException("Exception raised while trying to access counter file.");
        }

        // If the counter file didn't exist before,
        // set the counter to 0
        if (!didCounterFileExist) {
            setCounter(0);
        }

    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Read the count stored within the counter file.
     *
     * @return The guess count.
     */
    public long getCounter() {

        // Return -1 if there's an issue
        long counter = -1;

        try {
            synchronized (raf) {
                // Go to the beginning of the file
                // and read the long
                raf.seek(0);
                counter = raf.readLong();
            }
        } catch (Exception e) {
            logger.error("Exception raised while attempting to read counter file.", e);
        }

        return counter;

    }

    /**
     * Simply attempts a clean shutdown for
     * the RandomAccessFile, which shouldn't
     * be particularly necessary because the mode
     * we use necessitates that all write
     * operations block until they're done on
     * the storage device.
     */
    public void shutdown() {
        try {
            this.raf.close();
        } catch (Exception e) {
            // Don't care.
        }
    }

    /**
     * Writes the guess count within the
     * counter file.
     *
     * @param newValue The new guess count.
     */
    public void setCounter(long newValue) {
        try {
            synchronized (raf) {
                // Go to the beginning of the file
                // and write the long. Don't need to
                // worry about partial overwriting
                // because the long size is constant.
                raf.seek(0);
                raf.writeLong(newValue);
            }
        } catch (Exception e) {
            logger.error("Exception raised while attempting to write to the counter file.", e);
        }
    }

    /* ---------- PRIVATE METHODS ---------- */

    /**
     * Creates a new counter file. Will throw a
     * RuntimeException if we couldn't create it.
     * Assumes that the counter file doesn't
     * already exist.
     */
    private void createCounterFile() {

        boolean success = false;

        try {
            if (counterFile.createNewFile()) {
                // If we got here, we succeeded
                success = true;
            }
        } catch (Exception e) {
            logger.error("Exception raised while attempting to create counter file.", e);
        }

        if (!success) {
            throw new RuntimeException("Shutting down, no counter file present.");
        }

    }

}
