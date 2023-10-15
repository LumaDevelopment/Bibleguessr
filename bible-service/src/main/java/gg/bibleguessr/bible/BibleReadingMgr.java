package gg.bibleguessr.bible;

import gg.bibleguessr.bible.objs.Book;
import gg.bibleguessr.bible.objs.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
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
    * BibleService instance, mostly used to
    * access config.
    */
   private final BibleService service;

   /**
    * All Bible text, stored as a map from Version
    * to an array of all verses.
    */
   private final Map<Version, String[]> bibleText;

   /* ---------- CONSTRUCTOR ---------- */

   /**
    * Creates a new BibleReadingMgr object.
    *
    * @param service The BibleService instance.
    */
   public BibleReadingMgr(BibleService service) {

      this.logger = LoggerFactory.getLogger(LOGGER_NAME);
      this.service = service;
      this.bibleText = new HashMap<>();

      initializeText();

   }

   /* ---------- PUBLIC METHODS ---------- */

   /**
    * Gets a HashSet of all Bible versions that
    * this class has read.
    *
    * @return A HashSet of all Bible versions that
    * this class has read.
    */
   public HashSet<Version> getBibleVersions() {
      return new HashSet<>(bibleText.keySet());
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

   /* ---------- PRIVATE METHODS ---------- */

   /**
    * Reads through all versions of the Bible. Creates a
    * Version object for every version of the Bible.
    * Extracts the verse text and book names from every
    * version of the Bible. Stores all extracted information
    * for easy retrieval.
    */
   private void initializeText() {

      // Regex to pull the book name from reference
      Pattern pattern = Pattern.compile("^(\\D+)");

      // Get the length of the bible file extension
      int bibleFileExtensionLength = service.getConfig().bibleFileExtension().length() + 1;

      // Loop through all version files
      for (File versionFile : service.getBibleFiles()) {

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

               // The first part of the line is structured like
               // "Genesis 1:1", so get the book name out of this
               // by getting the substring of the first part of the
               // line until the last part of the reference, split
               // by spaces.
               Matcher matcher = pattern.matcher(lineParts[0]);
               String bookName = matcher.group(1).trim();

               // Update book name if it is different
               if(!bookName.equals(currentBookName)) {

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
               verseText[verseIndex] = lineParts[1];
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

         } catch (IOException e) {
            logger.error("Cannot read bible version: " + versionName, e);
         }

      }

   }

}
