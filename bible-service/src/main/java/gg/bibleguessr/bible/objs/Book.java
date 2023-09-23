package gg.bibleguessr.bible.objs;

public class Book {

    /* ---------- CONSTANTS ---------- */

    public static final Book NO_BOOK = new Book("", "", new Integer[]{}, null, null);

    /* ---------- VARIABLES ---------- */

    private final String name;
    private final String abbreviation;
    private final Chapter[] chapters;
    private final Book previous;
    private final Book next;

    /* ---------- CONSTRUCTORS ---------- */

    public Book(String name, String abbreviation, Integer[] versesPerChapter, Book previous, Book next) {

        this.name = name;
        this.abbreviation = abbreviation;
        this.chapters = new Chapter[versesPerChapter.length];
        this.previous = previous;
        this.next = next;

        initializeChapters(versesPerChapter);

    }

    /* ---------- GETTERS ---------- */

    public String abbreviation() {
        return abbreviation;
    }

    public Chapter chapter(int chapterNumber) {

        int index = chapterNumber - 1;

        if (index < 0 || index > chapters.length - 1) {
            return null;
        }

        return chapters[index];

    }

    public Chapter[] allChapters() {
        return chapters;
    }

    public Chapter firstChapter() {
        return chapters[0];
    }

    public Chapter lastChapter() {
        return chapters[chapters.length - 1];
    }

    public String name() {
        return name;
    }

    public Book nextBook() {
        return next;
    }

    public int numOfChapters() {
        return chapters.length;
    }

    public Book previousBook() {
        return previous;
    }

    public String reference() {
        return abbreviation;
    }

    /* ---------- PRIVATE METHODS ---------- */

    private void initializeChapters(Integer[] versesPerChapter) {
        for (int i = 0; i < versesPerChapter.length; i++) {
            chapters[i] = new Chapter(this, i + 1, versesPerChapter[i]);
        }
    }

    /* ---------- OBJECT OVERRIDES ---------- */

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Book && name.equals(((Book) obj).name());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

}
