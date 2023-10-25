package gg.bibleguessr.backend_utils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A class that contains utility methods for the Bibleguessr
 * project.
 */
public class BibleguessrUtilities {

    /**
     * The name of the logger used by this class. There is no
     */
    public static final String LOGGER_NAME = BibleguessrUtilities.class.getSimpleName();

    public static Map<String, String> convertObjNodeToStringMap(ObjectNode node) {

        // Get logging object, the map we read into,
        // and the fields that we'll be reading
        Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
        Map<String, String> parameters = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {

            // Iterate through every field
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode value = field.getValue();

            if (value.isValueNode()) {
                // Insert value into parameters map as a string
                parameters.put(field.getKey(), field.getValue().asText());
            } else if (value.isContainerNode()) {
                try {
                    // Convert object/array to JSON string and
                    // insert it into the parameters map
                    parameters.put(field.getKey(), GlobalObjectMapper.get().writeValueAsString(field.getValue()));
                } catch (Exception e) {
                    logger.error("Encountered an error while attempting to represent the \"" + field.getKey() + "\" " +
                            "field as a JSON string!", e);
                }
            }

        }

        return parameters;

    }

    /**
     * Attempts to read the provided JSON configuration file,
     * which represents the given configuration class, and
     * give a configuration object of the class. If the
     * configuration file does not exist, the default will
     * be created if the configClass has a method with
     * this header, where CONFIG_CLASS is the actual
     * config class:<br><br>
     * <code>public static CONFIG_CLASS getDefault()</code>
     *
     * @return The configuration object if successful, null otherwise.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getConfigObjFromFile(File configFile, Class<T> configClass) {

        T config;

        // Create objects for logging and JSON reading
        Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

        ObjectMapper mapper = GlobalObjectMapper.get();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {

            // Attempt to read configuration from file
            config = mapper.readValue(configFile, configClass);

            // If we made it to this point, it was successful, return config!
            return config;

        } catch (FileNotFoundException noConfigFileEx) {

            // No config file exists, so attempt to create a default.

            try {
                config = (T) configClass.getMethod("getDefault").invoke(null);
            } catch (Exception e) {
                logger.error(configClass.getSimpleName() + " doesn't have a getDefault() method, so no default configuration was made.");
                return null;
            }

            // Check if the method successfully made a config object.
            if (config == null) {
                logger.error(configClass.getSimpleName() + ".getDefault() returned null, so no default configuration was made.");
                return null;
            }

            try {

                // Attempt to write default configuration to file
                mapper.writeValue(configFile, config);

                // Could write configuration file, inform user.
                logger.info("Successfully wrote default configuration file!");

                return config;

            } catch (Exception writeConfigEx) {

                logger.error("Error while attempting to write default config file!", writeConfigEx);
                return null;

            }

        } catch (StreamReadException e) {

            logger.error("Configuration file is not formatted as JSON!", e);
            return null;

        } catch (DatabindException e) {

            logger.error("Configuration file does not match the format of " + configClass.getSimpleName() + "!", e);
            return null;

        } catch (IOException e) {

            logger.error("Couldn't read the configuration file!", e);
            return null;

        }

    }

    /**
     * Get the amount of memory currently in
     * use by the program.
     *
     * @return The memory in use, in bytes.
     */
    public static long getMemoryInUse() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

}
