package gg.bibleguessr.bible.objs;

import gg.bibleguessr.bible.Bible;
import gg.bibleguessr.bible.BibleService;

import java.util.Map;

public class Version {

   /* -------- INSTANCE VARIABLES -------- */

   private final String name;
   private final Map<Book, String> bookNames;

   /* -------- CONSTRUCTOR -------- */

   /**
    * Create a new version object.
    *
    * @param name      The name of the version.
    * @param bookNames A map from Book object to this version's
    *                  name for that book.
    */
   public Version(String name, Map<Book, String> bookNames) {

      // Validate the arguments
      if (name == null || bookNames == null) {
         throw new RuntimeException("Version name and book names must not be null!");
      }

      // Validate we have the right number of book names
      if (bookNames.size() != BibleService.BOOKS_IN_BIBLE) {
         throw new RuntimeException(
               "Version must have " + BibleService.BOOKS_IN_BIBLE + " books!");
      }

      // Validate that no book names are null
      for (Map.Entry<Book, String> entry : bookNames.entrySet()) {

         if (entry == null || entry.getKey() == null || entry.getValue() == null) {
            throw new RuntimeException("No entries, keys, or values of the book names map " +
                  "for a version can be null!");
         }
      }

      // Set instance variables
      this.name = name;
      this.bookNames = bookNames;

   }

   /* -------- PUBLIC METHODS -------- */

   /**
    * Get the name of this version of the Bible.
    *
    * @return The version's name
    */
   public String getName() {
      return name;
   }

   /**
    * Gets the name of the book with the given index
    * in the bible (Genesis = 0, Exodus = 1, etc.)
    *
    * @param bookIndex The index of the book in the bible
    * @return The name of the book, or <code>null</code>
    * if the book index is invalid.
    */
   public String getBookNameByIndex(int bookIndex) {

      Book book = Bible.getInstance().getBookByIndex(bookIndex);

      if (book == null) {
         return null;
      }

      return bookNames.get(book);

   }

   /**
    * Get the name of the book of the Bible associated
    * with the given Book object, according to this
    * version of the Bible.
    *
    * @return The name of the book, or <code>null</code>
    * if the book object is null.
    */
   public String getBookNameByObject(Book book) {

      if (book == null) {
         return null;
      }

      return bookNames.get(book);

   }

   /* -------- OVERRIDDEN METHODS -------- */

   @Override
   public boolean equals(Object obj) {

      if (!(obj instanceof Version otherVersion)) {
         return false;
      }

      return name.equals(otherVersion.name) && bookNames.equals(otherVersion.bookNames);

   }

   @Override
   public int hashCode() {
      return name.hashCode();
   }

}
