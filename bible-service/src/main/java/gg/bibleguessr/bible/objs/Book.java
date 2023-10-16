package gg.bibleguessr.bible.objs;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A class that represents a Book of the Bible.
 */
public class Book implements Comparable<Book> {

    /* ---------- CONSTANTS ---------- */

    /**
     * Represents the lack of a book for previous/next methods.
     */
    public static final Book NO_BOOK = new Book(
            new Integer[]{},
            -1,
            -1
    );

    /* ---------- VARIABLES ---------- */

    /**
     * An array of chapter objects representing
     * each chapter in this book.
     */
    private final Chapter[] chapters;

    /**
     * The universal index of the first verse of this book.
     */
    private final int firstVerseUniversalIndex;

    /**
     * The index of this book relative to the
     * rest of the Bible (Genesis = 0,
     * Exodus = 1, etc.)
     */
    private final int index;

    /**
     * The book that comes before this book in
     * the Bible, or NO_BOOK if this book is
     * Genesis.
     */
    private Book previous;

    /**
     * The book that comes after this book in
     * the Bible, or NO_BOOK if this book is
     * Revelation.
     */
    private Book next;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Creates a new book object with the given parameters.
     * Previous and next books still need to be set after
     * creation.
     *
     * @param versesPerChapter         An array of the number of verses
     *                                 in each chapter of this book (also
     *                                 inherently informs the number
     *                                 of chapters in the book)
     * @param firstVerseUniversalIndex The universal index of the first verse
     *                                 of this book.
     * @param index                    The index of this book in comparison to the
     *                                 rest of the books of the Bible [0, 65], where
     *                                 Genesis = 0, Exodus = 1, etc.
     */
    public Book(Integer[] versesPerChapter, int firstVerseUniversalIndex, int index) {

        if (versesPerChapter == null) {
            throw new RuntimeException("Verses per chapter array cannot be null when " +
                    "instantiating a Book object!");
        }

        this.chapters = new Chapter[versesPerChapter.length];
        this.firstVerseUniversalIndex = firstVerseUniversalIndex;
        this.index = index;

        initializeChapters(versesPerChapter);

    }

    /* ---------- METHODS ---------- */

    /**
     * Get the chapter of this book with the given number.
     * (Example: Let the genesis variable represent the book
     * of Genesis. genesis.chapter(1) would be the first chapter
     * of Genesis, Genesis 1)
     *
     * @param chapterNumber The number of the chapter to get (not the index)
     * @return The chapter of this book with the given number, or
     * <code>null</code> if the chapter number is invalid.
     */
    public Chapter chapter(int chapterNumber) {

        int index = chapterNumber - 1;

        if (index < 0 || index > chapters.length - 1) {
            return null;
        }

        return chapters[index];

    }

    /**
     * Retrieves and returns all chapters belonging to this book.
     *
     * @return An array of all Chapter objects of this book, or
     * <code>null</code> if this book is the "NO_BOOK".
     */
    public Chapter[] allChapters() {

        if (chapters.length == 0) {
            return null;
        }

        return chapters;

    }

    /**
     * Retrieves the first chapter of this book.
     *
     * @return A Chapter object representing the
     * first chapter of this book, or <code>null</code>
     * if this is the "NO_BOOK".
     */
    public Chapter firstChapter() {

        if (chapters.length < 1) {
            // If someone calls NO_BOOK.firstChapter()
            return null;
        }

        return chapters[0];

    }

    /**
     * The index of this book relative to all
     * other books in the Bible (for instance,
     * Genesis would be 0, Exodus would be 1, etc.)
     *
     * @return The index of this book
     */
    public int index() {
        return index;
    }

    /**
     * Determines whether this book is the "NO_BOOK"
     * (the Book object that is returned when calling
     * <code>previousBook()</code> on Genesis and
     * when calling <code>nextBook()</code> on
     * Revelation)
     *
     * @return Whether this book is the "NO_BOOK"
     */
    public boolean isNotABook() {
        return this.equals(NO_BOOK);
    }

    /**
     * Retrieves the last chapter of this book.
     *
     * @return A Chapter object representing the
     * last chapter of this book, or <code>null</code>
     * if this is the "NO_BOOK".
     */
    public Chapter lastChapter() {

        if (chapters.length < 1) {
            return null;
        }

        return chapters[chapters.length - 1];

    }

    /**
     * Gets the Book object of the book
     * that comes after this one in the
     * Bible.
     *
     * @return The Book object, or the
     * "NO_BOOK" object if no book comes
     * after this one.
     */
    public Book nextBook() {
        if (next != null) {
            return next;
        } else {
            return NO_BOOK;
        }
    }

    /**
     * Determines whether this book is in the New Testament
     * or not based on its index.
     *
     * @return Whether this book is in the New Testament.
     */
    public boolean newTestament() {
        return index >= 39;
    }

    /**
     * Determines the number of chapters in this book.
     *
     * @return The number of chapters in this book.
     */
    public int numOfChapters() {
        return chapters.length;
    }

    /**
     * Determines whether this book is in the Old Testament
     * or not based on its index.
     *
     * @return Whether this book is in the Old Testament.
     */
    public boolean oldTestament() {
        return index < 39;
    }

    /**
     * Gets the Book object of the book
     * that comes before this one in the
     * Bible.
     *
     * @return The Book object, or the
     * "NO_BOOK" object if no book comes
     * before this one.
     */
    public Book previousBook() {
        if (previous != null) {
            return previous;
        } else {
            return NO_BOOK;
        }
    }

    /**
     * Sets the Book object that comes after this one in the Bible.
     *
     * @param next The Book object that comes after this one in the Bible.
     */
    public void setNext(Book next) {
        this.next = next;
    }

    /**
     * Sets the Book object that comes before this one in the Bible.
     *
     * @param previous The Book object that comes before this one in the Bible.
     */
    public void setPrevious(Book previous) {
        this.previous = previous;
    }

    /* ---------- PRIVATE METHODS ---------- */

    /**
     * Initializes the chapters array by creating new
     * Chapter objects for each chapter in this book (the
     * verses per chapter and provided by the given array).
     *
     * @param versesPerChapter An array of the number of verses
     *                         in each chapter of this book (also
     *                         inherently informs the number
     *                         of chapters in the book)
     */
    private void initializeChapters(Integer[] versesPerChapter) {

        // Calculate the universal index of the first
        // verse of every chapter using the universal
        // index of the first verse of this book
        // and the number of verses of each chapter
        // in the book
        Integer[] universalIndicesForChapters = new Integer[versesPerChapter.length];

        for (int i = 0; i < universalIndicesForChapters.length; i++) {
            if (i == 0) {
                universalIndicesForChapters[i] = firstVerseUniversalIndex;
            } else {
                universalIndicesForChapters[i] =
                        universalIndicesForChapters[i - 1] + versesPerChapter[i - 1];
            }
        }

        // Create a new Chapter object for every
        // chapter in the book.
        for (int i = 0; i < versesPerChapter.length; i++) {
            chapters[i] = new Chapter(
                    this,
                    universalIndicesForChapters[i],
                    i + 1,
                    versesPerChapter[i]
            );
        }

    }

    /* ---------- OVERRIDDEN METHODS ---------- */

    @Override
    public int compareTo(@NotNull Book o) {
        return Integer.compare(this.index(), o.index());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Book && Arrays.equals(chapters, ((Book) obj).chapters);
    }

    @Override
    public int hashCode() {
        return index;
    }

}
