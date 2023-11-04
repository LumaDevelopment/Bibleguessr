export class Verse {
    bibleVersion!: string;
    bookName!: string;
    chapter!: number;
    verseNumber!: number;
    globalVerseNumber: number;
    text!: string
    constructor(bibleVersion: string, bookName: string, chapter: number, verseNumber: number, globalVerseNumber: number, text: string) {
        this.bibleVersion = bibleVersion;
        this.bookName = bookName;
        this.chapter = chapter;
        this.verseNumber = verseNumber;
        this.globalVerseNumber = globalVerseNumber;
        this.text = text;
    }
}