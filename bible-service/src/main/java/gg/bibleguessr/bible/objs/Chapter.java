package gg.bibleguessr.bible.objs;

public class Chapter {

    /* ---------- VARIABLES ---------- */

    private final Book book;
    private final int number;
    private final Verse[] verses;

    /* ---------- CONSTRUCTORS ---------- */

    public Chapter(Book book, int number, int numVerses) {

        this.book = book;
        this.number = number;
        this.verses = new Verse[numVerses];

        initializeVerses();

    }

    /* ---------- GETTERS ---------- */

    public Verse[] allVerses() {
        return verses;
    }

    public Book book() {
        return book;
    }

    public Verse firstVerse() {
        return verses[0];
    }

    public Verse lastVerse() {
        return verses[verses.length - 1];
    }

    public Chapter previousChapter() {
        return number == 1 ? book.previousBook().lastChapter() : book.chapter(number);
    }

    public Chapter nextChapter() {
        return number == book.numOfChapters() ? book.nextBook().firstChapter() : book.chapter(number + 1);
    }

    public int number() {
        return number;
    }

    public int numOfVerses() {
        return verses.length;
    }

    public String reference() {
        return book.reference() + "." + String.format("%03d", number);
    }

    public Verse verse(int verseNumber) {

        int index = verseNumber - 1;

        if (index < 0 || index > verses.length - 1) {
            return null;
        }

        return verses[verseNumber - 1];

    }

    /* ---------- PRIVATE METHODS ---------- */

    private void initializeVerses() {
        for (int i = 0; i < verses.length; i++) {
            verses[i] = new Verse(this, i + 1);
        }
    }

    /* ---------- OBJECT OVERRIDES ---------- */

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Chapter chapter) {
            return chapter.book.equals(book) && chapter.number == number();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return book.name() + " " + number;
    }

}
