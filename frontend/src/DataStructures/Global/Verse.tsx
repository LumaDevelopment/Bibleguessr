import { BibleVersion } from "./BibleVersion";
import { BookName } from "./BookName";

export class Verse {
    bibleVersion!: BibleVersion;
    bookName!: BookName;
    chapter!: number;
    verseNumber!: number;
    globalVerseNumber: number;
    text!: string
    constructor(bibleVersion: BibleVersion, bookName: BookName, chapter: number, verseNumber: number, globalVerseNumber: number, text: string) {
        this.bibleVersion = bibleVersion;
        this.bookName = bookName;
        this.chapter = chapter;
        this.verseNumber = verseNumber;
        this.globalVerseNumber = globalVerseNumber;
        this.text = text;
    }
}