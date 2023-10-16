export class Verse {
    bibleVersion!: String;
    bookName!: String;
    chapter!: number;
    verseNumber!: number;
    globalVerseNumber: number;
    text!: string
    constructor(bibleVersion: String, bookName: String, chapter: number, verseNumber: number, globalVerseNumber: number, text: string) {
        this.bibleVersion = bibleVersion;
        this.bookName = bookName;
        this.chapter = chapter;
        this.verseNumber = verseNumber;
        this.globalVerseNumber = globalVerseNumber;
        this.text = text;
    }
}