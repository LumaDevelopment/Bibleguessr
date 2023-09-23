package gg.bibleguessr.bible.objs;

import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a Verse in a Chapter
 * in a Book of the Bible.
 *
 * @param chapter The Chapter object that this Verse belongs to.
 * @param number  The number of this Verse in its Chapter.
 */
public record Verse(Chapter chapter, int number) implements Comparable<Verse> {

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Creates a new Verse object with the given parameters.
     *
     * @param chapter The Chapter object that this Verse belongs to.
     * @param number  The number of this Verse in its Chapter.
     */
    public Verse {
        if (chapter == null) {
            throw new RuntimeException("Chapter object in the Verse constructor cannot be null!");
        }
    }

    /* ---------- GETTERS ---------- */

    /**
     * Returns the Verse object of the verse that comes
     * after this one in the Bible.
     *
     * @return The Verse object of the verse that comes
     * after this one in the Bible, or <code>null</code>
     * if this verse is Revelation 22:21.
     */
    public Verse nextVerse() {

        int nextVerseNumber = number + 1;

        if (nextVerseNumber > chapter.numOfVerses()) {

            // If the next verse is in the next chapter of
            // the Bible, ensure that the next chapter exists,
            // and if so, return the first verse of it.
            // If this verse is Revelation 22:21, return null.

            Chapter nextChapter = chapter.nextChapter();

            if (nextChapter != null) {
                return nextChapter.firstVerse();
            } else {
                return null;
            }

        } else {
            return chapter.verse(nextVerseNumber);
        }

    }

    /**
     * Returns the Verse object of the verse that comes
     * before this one in the Bible.
     *
     * @return The Verse object of the verse that comes
     * before this one in the Bible, or <code>null</code>
     * if this verse is Genesis 1:1.
     */
    public Verse previousVerse() {

        int previousVerseNumber = number - 1;

        if (previousVerseNumber < 1) {

            // If the previous verse is in the previous chapter of
            // the Bible, ensure that the previous chapter exists,
            // and if so, return the last verse of it.
            // If this verse is Genesis 1:1, return null.

            Chapter previousChapter = chapter.previousChapter();

            if (previousChapter != null) {
                return previousChapter.lastVerse();
            } else {
                return null;
            }

        } else {
            return chapter.verse(previousVerseNumber);
        }

    }

    /**
     * Retrieves the reference of this verse, where the
     * reference is the OSIS reference of this verse's book
     * concatenated with the number of this verse's chapter
     * and this verse's number (both 0 padded to length 3)
     * by a period. Example of Genesis 1:1:<br>
     * <code>GEN.001.001</code>
     *
     * @return The reference of this verse.
     */
    public String reference() {
        return chapter.reference() + "." + String.format("%03d", number);
    }

    /* ---------- OVERRIDDEN METHODS ---------- */

    /**
     * Compares this Verse object to another Verse object by<br>
     * 1) checking for equality<br>
     * 2) comparing by Book index<br>
     * 3) comparing by Chapter number<br>
     * 4) comparing by Verse number<br>
     *
     * @param otherVerse the object to be compared.
     * @return a negative integer, zero, or a positive integer depending on
     * if this object is less than, equal to, or greater than the specified
     * object, respectively.
     */
    @Override
    public int compareTo(@NotNull Verse otherVerse) {

        int chapterCompareVal = chapter().compareTo(otherVerse.chapter());

        if (chapterCompareVal != 0) {
            return chapterCompareVal;
        }

        return Integer.compare(this.number(), otherVerse.number());

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Verse verse) {
            return verse.chapter.equals(chapter) && verse.number == number;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return chapter.toString() + ":" + number;
    }

}
