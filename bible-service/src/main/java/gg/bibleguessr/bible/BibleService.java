package gg.bibleguessr.bible;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BibleService extends Microservice {

    /* ---------- CONSTANTS ---------- */

    public static final String LOGGER_NAME = BibleService.class.getSimpleName();
    public static final String DEFAULT_CONFIG_FILE_PATH = "bible_service_config.json";

    /* ---------- VARIABLES ---------- */

    private final Logger logger;
    private final File configFile;
    private BibleServiceConfig config;

    /* ---------- CONSTRUCTORS ---------- */

    public BibleService() {
        this(new File(DEFAULT_CONFIG_FILE_PATH));
    }

    public BibleService(File configFile) {

        // Tell the Service Wrapper that the ID
        // of this service is "bible"
        super("bible");

        this.logger = LoggerFactory.getLogger(LOGGER_NAME);
        this.configFile = configFile;
        this.config = null;

        initializeService();

    }


    /* ---------- METHODS ---------- */

    @Override
    public Response executeRequest(Request request) {
        return null;
    }

    /**
     * Opens the specified bibles directory and retrieves
     * all valid Bible files.
     *
     * @return Array of Bible files, or <code>null</code>
     * if there was an issue creating/accessing the directory.
     */
    public File[] getBibleFiles() {

        // Establish the directory and attempt to
        // create it if it doesn't exist
        File biblesDirectory = new File(config.biblesDirectory());
        boolean dirCreated = biblesDirectory.mkdirs();

        if (!dirCreated && !biblesDirectory.exists()) {
            // Directory couldn't be created
            logger.error("Could not create bibles directory!");
            return null;
        }

        // Create a filter to determine what files are valid
        FilenameFilter bibleFileFilter = (dir, name) -> name.endsWith("." + config.bibleFileExtension());

        // Get all valid bible files in the directory
        return biblesDirectory.listFiles(bibleFileFilter);

    }

    /**
     * Attempts to load the config file into the
     * config object. If the config file does not
     * exist, attempts to write the default config.
     *
     * @return Whether the initialization was successful.
     */
    public boolean initializeConfig() {

        if (config != null) {
            // Configuration is already present.
            return true;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {

            // Attempt to read configuration from file
            config = mapper.readValue(configFile, BibleServiceConfig.class);

            // If we made it to this point, it was successful, so do not halt
            return true;

        } catch (Exception readConfigEx) {

            // Couldn't read from file, so get default config
            // and attempt to write
            config = BibleServiceConfig.getDefault();

            try {

                // Attempt to write default configuration to file
                mapper.writeValue(configFile, config);

                // Could write configuration file, inform user.
                logger.info("Successfully wrote default configuration file. Halting program, modify and restart.");

            } catch (Exception writeConfigEx) {

                // Couldn't write default configuration, inform user.
                logger.error("Error while attempting to write default config file!", writeConfigEx);

            }

        }

        return false;

    }

    @Override
    public void initializeRequestTypesMap() {

    }

    private void initializeService() {

        // Attempt to initialize the configuration,
        // and throw an error if we can't
        if (!initializeConfig()) {
            throw new RuntimeException("Configuration file does not exist, see logs for more information.");
        }

        // Get all valid Bible files
        File[] bibleFiles = getBibleFiles();

        if (bibleFiles == null) {
            throw new RuntimeException("Could not get Bible files, see logs for more information.");
        }

        // TODO things here

    }

    @Override
    public void shutdown() {

    }

}