package gg.bibleguessr.bible;

import gg.bibleguessr.bible.objs.Version;
import gg.bibleguessr.service_wrapper.Microservice;
import gg.bibleguessr.service_wrapper.Request;
import gg.bibleguessr.service_wrapper.Response;
import gg.bibleguessr.service_wrapper.ServiceUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;

/**
 * The BibleService class is the main class for the
 * Bible microservice.
 */
public class BibleService extends Microservice {

   /* ---------- CONSTANTS ---------- */

   /**
    * The name of the logger used by this class
    * (the simple name of this class).
    */
   public static final String LOGGER_NAME = BibleService.class.getSimpleName();

   /**
    * The default path to the config file.
    */
   public static final String DEFAULT_CONFIG_FILE_PATH = "bible_service_config.json";

   // Bible numerical constants
   public static final int BOOKS_IN_BIBLE = 66;
   public static final int VERSES_IN_BIBLE = 31_102;

   /* ---------- VARIABLES ---------- */

   /**
    * The logger this class uses to record its
    * status and errors.
    */
   private final Logger logger;

   /**
    * The config file for this service.
    */
   private final File configFile;

   /**
    * The config object for this service.
    */
   private BibleServiceConfig config;

   /**
    * The class that operates reading text from different
    * Bible versions.
    */
   private BibleReadingMgr bibleReadingMgr;

   /**
    * Versions offered by this service, found out
    * by the bible reading manager.
    */
   private HashSet<Version> versions;

   /* ---------- CONSTRUCTORS ---------- */

   /**
    * Creates a new BibleService object with the
    * default config file path. Calls BibleService(configFile).
    */
   public BibleService() {
      this(new File(DEFAULT_CONFIG_FILE_PATH));
   }

   /**
    * Creates a new BibleService object with the
    * specified config file. Initializes the service, which
    * includes managing the config file and Bible versions.
    *
    * @param configFile The config file for this service.
    */
   public BibleService(File configFile) {

      // Tell the Service Wrapper that the ID
      // of this service is "bible"
      super("bible");

      this.logger = LoggerFactory.getLogger(LOGGER_NAME);
      this.configFile = configFile;
      this.config = null;
      this.bibleReadingMgr = null;
      this.versions = null;

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
      FilenameFilter bibleFileFilter =
            (dir, name) -> name.endsWith("." + config.bibleFileExtension());

      // Get all valid bible files in the directory
      return biblesDirectory.listFiles(bibleFileFilter);

   }

   /**
    * Gets the BibleReadingMgr object for this service.
    *
    * @return The BibleReadingMgr object for this service.
    */
   public BibleReadingMgr getBibleReadingMgr() {
      return bibleReadingMgr;
   }

   /**
    * Gets the config object for this service.
    *
    * @return The config object for this service.
    */
   public BibleServiceConfig getConfig() {
      return config;
   }

   /**
    * Gets all Bible versions accessible by
    * this service.
    *
    * @return All Bible versions accessible by
    * this service.
    */
   public HashSet<Version> getVersions() {
      return versions;
   }

   /**
    * Checks if the config object already exists,
    * and if so, returns it. Otherwise, attempts
    * to read the config file and create the
    * config object. Additionally, if the config
    * file doesn't exist and the config class has
    * a getDefault() method, it will be called
    * and the default config will be written to
    * the config file.
    *
    * @return Whether the config now is set
    * correctly.
    */
   public boolean initializeConfig() {

      if (config != null) {
         return true;
      }

      // Attempt to create the config.
      config = ServiceUtilities.getConfigObjFromFile(configFile, BibleServiceConfig.class);

      return config != null;

   }

   @Override
   public void initializeRequestTypesMap() {

   }

   private void initializeService() {

      // Attempt to initialize the configuration,
      // and throw an error if we can't
      if (!initializeConfig()) {
         throw new RuntimeException(
               "Configuration file does not exist, see logs for more information.");
      }

      // Get all valid Bible files
      File[] bibleFiles = getBibleFiles();

      if (bibleFiles == null) {
         throw new RuntimeException(
               "Could not get Bible files, see logs for more information.");
      }

      this.bibleReadingMgr = new BibleReadingMgr(this);
      this.versions = bibleReadingMgr.getBibleVersions();

   }

   @Override
   public void shutdown() {

   }

}