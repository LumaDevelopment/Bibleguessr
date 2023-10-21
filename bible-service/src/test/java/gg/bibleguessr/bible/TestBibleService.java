package gg.bibleguessr.bible;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gg.bibleguessr.bible.data_structures.Book;
import gg.bibleguessr.bible.data_structures.Chapter;
import gg.bibleguessr.bible.data_structures.Verse;
import gg.bibleguessr.bible.data_structures.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBibleService {

    private BibleService service = null;
    private Bible bible = null;

    @BeforeEach
    void initVars() {
        if (service == null) {
            service = new BibleService();
        }
        if (bible == null) {
            bible = Bible.getInstance();
        }
    }

    @Test
    @DisplayName("No Null Books")
    void noNullBooks() {
        for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {
            Book book = bible.getBookByIndex(i);
            assertNotNull(book);
        }
    }

    @Test
    @DisplayName("No Null Chapters")
    void noNullChapters() {
        for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {
            Book book = bible.getBookByIndex(i);
            assertNotNull(book);

            for (int j = 0; j < book.numOfChapters(); j++) {
                assertNotNull(book.chapter(j + 1));
            }

        }
    }

    @Test
    @DisplayName("No Null Verses")
    void noNulLVerses() {

        // Can test this in two ways. First, by hierarchical traversal:
        for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {
            Book book = bible.getBookByIndex(i);
            assertNotNull(book);

            for (int j = 0; j < book.numOfChapters(); j++) {

                Chapter chapter = book.chapter(j + 1);
                assertNotNull(chapter);

                for (int k = 0; k < chapter.numOfVerses(); k++) {
                    assertNotNull(chapter.verse(k + 1));
                }

            }

        }

        // Next, by universal index
        for (int i = 0; i < Bible.NUM_OF_VERSES; i++) {
            assertNotNull(bible.getVerseByUniversalIndex(i));
        }

    }

    @Test
    @DisplayName("Bible Constructed Correctly")
    void bibleConstructedCorrectly() {

        for (int bookIndex = 0; bookIndex < Bible.VERSES_PER_CHAPTER.length; bookIndex++) {
            for (int chapterNum = 1; chapterNum <= Bible.VERSES_PER_CHAPTER[bookIndex].length; chapterNum++) {
                for (int verseNum = 1; verseNum <= Bible.VERSES_PER_CHAPTER[bookIndex][chapterNum - 1]; verseNum++) {
                    assertNotNull(bible.getVerseByReference(bookIndex, chapterNum, verseNum));
                }
            }
        }

    }

    @Test
    @DisplayName("Bible Object Internal Info Is Correct")
    void bibleObjectInternalInfoIsCorrect() {

        for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {
            Book book = bible.getBookByIndex(i);
            assertEquals(i, book.index());

            for (int j = 0; j < book.numOfChapters(); j++) {

                Chapter chapter = book.chapter(j + 1);
                assertEquals(j + 1, chapter.number());

                for (int k = 0; k < chapter.numOfVerses(); k++) {
                    assertEquals(k + 1, chapter.verse(k + 1).number());
                }

            }

        }

    }

    @Test
    @DisplayName("Can Traverse Bible")
    void canTraverseBible() {

        // Attempt to traverse all the way from Genesis 1:1 to Revelation 22:21
        Verse firstVerseOfBible = bible.getVerseByReference(0, 1, 1);

        Verse previousVerse = null;
        Verse currentVerse = firstVerseOfBible;

        while (currentVerse != null) {
            previousVerse = currentVerse;
            currentVerse = currentVerse.nextVerse();
        }

        // Previous verse should be Revelation 22:21
        Verse lastVerseOfBible = bible.getVerseByReference(65, 22, 21);

        assertEquals(lastVerseOfBible, previousVerse);

        // Now, work backwards, traversing from Revelation 22:21 to Genesis 1:1
        previousVerse = null;
        currentVerse = lastVerseOfBible;

        while (currentVerse != null) {
            previousVerse = currentVerse;
            currentVerse = currentVerse.previousVerse();
        }

        // Previous verse should be Genesis 1:1
        assertEquals(firstVerseOfBible, previousVerse);

    }

    @Test
    @DisplayName("Bible Iterators Work Correctly")
    void bibleIteratorsWorkCorrectly() {

        Iterator<Book> bookIterator = bible.getBookIterator();

        while (bookIterator.hasNext()) {
            assertNotNull(bookIterator.next());
        }

        Iterator<Verse> verseIterator = bible.getVerseIterator();

        while (verseIterator.hasNext()) {
            assertNotNull(verseIterator.next());
        }

    }

    @Test
    @DisplayName("Frontend Bible Data Is Correct")
    void frontendBibleDataIsCorrect() {

        ObjectNode data = service.getFrontendBibleDataMgr().getBibleData();
        Collection<Version> versions = service.getBibleVersionMgr().getVersions();

        // Verify the basics of the bible names array is correct
        JsonNode bibleNames = data.get("bibleNames");
        assertNotNull(bibleNames);
        assertTrue(bibleNames.isArray());
        assertEquals(versions.size(), bibleNames.size());

        // Verify the basics of the bible book names map is correct
        JsonNode bibleBookNames = data.get("bibleBookNames");
        assertNotNull(bibleBookNames);
        assertTrue(bibleBookNames.isObject());
        assertEquals(versions.size(), bibleBookNames.size());

        // Loop through every version, and make sure
        // its name is in bibleNames, and its book names
        // are represented accurately in bibleBookNames
        for (Version version :versions) {

            // Bible names should contain this version's name
            boolean foundVersionInBibleNames = false;

            for (JsonNode bibleName : bibleNames) {

                assertNotNull(bibleName);
                assertTrue(bibleName.isTextual());

                if (version.getName().equals(bibleName.asText())) {
                    foundVersionInBibleNames = true;
                    break;
                }

            }

            assertTrue(foundVersionInBibleNames);

            // Bible book names should contain this version's book names
            JsonNode versionBookNames = bibleBookNames.get(version.getName());
            assertNotNull(versionBookNames);
            assertTrue(versionBookNames.isArray());
            assertEquals(Bible.NUM_OF_BOOKS, versionBookNames.size());

            // Loop through both lists to check for equality
            for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {

                JsonNode versionBookName = versionBookNames.get(i);
                assertNotNull(versionBookName);
                assertTrue(versionBookName.isTextual());
                assertEquals(version.getBookNameByIndex(i), versionBookName.asText());

            }

        }

        // Make sure dataMatrix is equivalent to Bible.VERSES_PER_CHAPTER
        JsonNode dataMatrix = data.get("dataMatrix");
        assertNotNull(dataMatrix);
        assertTrue(dataMatrix.isArray());
        assertEquals(Bible.NUM_OF_BOOKS, dataMatrix.size());

        // Loop through books
        for (int i = 0; i < Bible.NUM_OF_BOOKS; i++) {

            JsonNode book = dataMatrix.get(i);
            assertNotNull(book);
            assertTrue(book.isArray());
            assertEquals(Bible.VERSES_PER_CHAPTER[i].length, book.size());

            // Loop through chapters
            for (int j = 0; j < Bible.VERSES_PER_CHAPTER[i].length; j++) {

                JsonNode chapter = book.get(j);
                assertNotNull(chapter);
                assertTrue(chapter.isInt());
                assertEquals(Bible.VERSES_PER_CHAPTER[i][j], chapter.asInt());

            }

        }

    }

}
