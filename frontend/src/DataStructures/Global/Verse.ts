import { Printable } from "./Printable";

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
    /**
     * Returns the bible verse in:
     * 
     * Bookname, chapter, verseNumber
     * 
     * order
     */
    getVerseIdentifier(): string {
      return this.bookName+", "+this.chapter+", "+this.verseNumber
    }
    /**
     * @param other 
     * @returns The distance in verse between this verse and another via globalVerseNumber
     */
    getDistance(other: Verse): number {
      return Math.abs(this.globalVerseNumber - other.globalVerseNumber)
    }
}