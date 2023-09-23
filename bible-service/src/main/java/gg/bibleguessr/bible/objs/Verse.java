package gg.bibleguessr.bible.objs;

public class Verse {

    /* ---------- VARIABLES ---------- */

    private final Chapter chapter;
    private final int number;

    /* ---------- CONSTRUCTORS ---------- */

    public Verse(Chapter chapter, int number) {
        this.chapter = chapter;
        this.number = number;
    }

    /* ---------- GETTERS ---------- */

    public Verse nextVerse() {

        int nextVerseNumber = number + 1;

        if (nextVerseNumber > chapter.numOfVerses()) {
            return chapter.nextChapter().firstVerse();
        } else {
            return chapter.verse(nextVerseNumber);
        }

    }

    public Verse previousVerse() {

        int previousVerseNumber = number - 1;

        if (previousVerseNumber < 1) {
            return chapter.previousChapter().lastVerse();
        } else {
            return chapter.verse(previousVerseNumber);
        }

    }

    public String reference() {
        return chapter.reference() + "." + String.format("%03d", number);
    }

    /* ---------- OBJECT OVERRIDES ---------- */

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
        return chapter.reference() + ":" + number;
    }

}
