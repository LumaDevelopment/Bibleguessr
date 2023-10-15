package gg.bibleguessr.bible.objs;

import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a Chapter in a Book of the Bible.
 */
public class Chapter implements Comparable<Chapter> {

   /* ---------- VARIABLES ---------- */

   /**
    * The Book object that this Chapter belongs to.
    */
   private final Book book;

   /**
    * The universal index of the first verse
    * of this chapter.
    */
   private final int firstVerseUniversalIndex;

   /**
    * The number of this Chapter in its Book.
    */
   private final int number;

   /**
    * The Verse objects that belong to this Chapter.
    * Inherently gives the number of verses in
    * this Chapter.
    */
   private final Verse[] verses;

   /* ---------- CONSTRUCTORS ---------- */

   /**
    * Creates a new chapter object with the given parameters.
    *
    * @param book                     The Book object that this Chapter belongs to.
    * @param firstVerseUniversalIndex The universal index of the first verse of this Chapter.
    * @param number                   The number of this Chapter in its Book.
    * @param numVerses                The number of verses in this Chapter.
    */
   public Chapter(Book book, int firstVerseUniversalIndex, int number, int numVerses) {

      if (book == null) {
         throw new RuntimeException("Book object in the Chapter constructor cannot be null!");
      }

      this.book = book;
      this.firstVerseUniversalIndex = firstVerseUniversalIndex;
      this.number = number;
      this.verses = new Verse[numVerses];

      initializeVerses();

   }

   /* ---------- GETTERS ---------- */

   /**
    * Retrieves and returns all verses belonging to this chapter.
    *
    * @return An array of Verse objects belonging to this chapter.
    */
   public Verse[] allVerses() {
      return verses;
   }

   /**
    * Retrieves and returns the Book object that this Chapter belongs to.
    *
    * @return The Book object that this Chapter belongs to.
    */
   public Book book() {
      return book;
   }

   /**
    * Retrieves the first verse of this chapter.
    *
    * @return A Verse object representing the
    * first verse of this chapter.
    */
   public Verse firstVerse() {
      return verses[0];
   }

   /**
    * Retrieves the last verse of this chapter.
    *
    * @return A Verse object representing the
    * last verse of this chapter.
    */
   public Verse lastVerse() {
      return verses[verses.length - 1];
   }

   /**
    * Retrieves the chapter that comes before this one in the
    * Bible.
    *
    * @return The Chapter object representing the chapter
    * that comes before this one in the Bible, or
    * <code>null</code> if this chapter is Genesis 1.
    */
   public Chapter previousChapter() {

      if (book.previousBook().isNotABook() && number == 1) {
         // This chapter is Genesis 1
         return null;
      }

      if (number == 1) {
         // This is the first chapter of this book, so
         // return the last chapter of the previous book.
         return book.previousBook().lastChapter();
      }

      // This is not the first chapter of this book, so
      // return the previous chapter of this book.
      return book.chapter(number - 1);
   }

   /**
    * Retrieves the chapter that comes after this one in the
    * Bible.
    *
    * @return The Chapter object representing the chapter
    * that comes after this one in the Bible, or
    * <code>null</code> if this chapter is Revelation 22.
    */
   public Chapter nextChapter() {

      if (book.nextBook().isNotABook() && number == book.numOfChapters()) {
         // This chapter is Revelation 22
         return null;
      }

      if (number == book.numOfChapters()) {
         // This is the last chapter of this book, so
         // return the first chapter of the next book.
         return book.nextBook().firstChapter();
      }

      // This is not the last chapter of this book, so
      // return the next chapter of this book.
      return book.chapter(number + 1);

   }

   /**
    * Retrieves the number of this Chapter in its Book.
    * This is <b>not</b> the index of the Chapter in its Book.
    *
    * @return The number of this Chapter in its Book.
    */
   public int number() {
      return number;
   }

   /**
    * Retrieves the number of verses in this Chapter.
    *
    * @return The number of verses in this Chapter.
    */
   public int numOfVerses() {
      return verses.length;
   }

   /**
    * Retrieves the Verse object of the verse with the given
    * verse number.
    *
    * @param verseNumber The number of the verse to retrieve.
    * @return The Verse object of the verse with the given
    * verse number, or <code>null</code> if the verse number
    * is invalid.
    */
   public Verse verse(int verseNumber) {

      int index = verseNumber - 1;

      if (index < 0 || index > verses.length - 1) {
         return null;
      }

      return verses[verseNumber - 1];

   }

   /* ---------- PRIVATE METHODS ---------- */

   /**
    * Initializes the Verse objects of this Chapter
    * by creating them and adding them to the verses array.
    */
   private void initializeVerses() {
      for (int i = 0; i < verses.length; i++) {
         verses[i] = new Verse(
               this,
               i + 1,
               firstVerseUniversalIndex + i
         );
      }
   }

   /* ---------- OVERRIDDEN METHODS ---------- */

   @Override
   public int compareTo(@NotNull Chapter o) {

      int bookCompareVal = book().compareTo(o.book());

      if (bookCompareVal != 0) {
         return bookCompareVal;
      }

      return Integer.compare(number(), o.number());

   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Chapter chapter) {
         return chapter.book.equals(book) && chapter.number == number;
      }
      return false;
   }

   @Override
   public int hashCode() {
      return (book.index() + "." + number).hashCode();
   }

}
