import { Subscribable } from "./Subscribable";

export class Verse extends Subscribable {
   private bibleVersion!: string;
   private bookName!: string;
   private chapter!: number;
   private verseNumber!: number;
   private globalVerseNumber: number;
   private text!: string
   constructor(bibleVersion: string, bookName: string, chapter: number, verseNumber: number, globalVerseNumber: number, text: string) {
      super()
      this.bibleVersion = bibleVersion;
      this.bookName = bookName;
      this.chapter = chapter;
      this.verseNumber = verseNumber;
      this.globalVerseNumber = globalVerseNumber;
      this.text = text;
   }

   // Setters

   setBibleVersion = (bibleVersion: string) => {
      this.bibleVersion = bibleVersion
      this.emitChange()
   }

   setBookName = (bookName: string) => {
      this.bookName = bookName;
      this.emitChange()
   }

   setChapter = (chapter: number) => {
      this.chapter = chapter;
      this.emitChange()
   }

   setVerseNumber = (verseNumber: number) => {
      this.verseNumber = verseNumber;
      this.emitChange()
   }

   setGlobalVerseNumber = (globalVerseNumber: number) => {
      this.globalVerseNumber = globalVerseNumber;
      this.emitChange();
   }

   setText = (text: string) => {
      this.text = text;
   }

   // Getters

   getBibleVersion = (): string => {
      return this.bibleVersion
   }

   getBookName = (): string => {
      return this.bookName;
   }

   getChapter = (): number => {
      return this.chapter;
   }

   getVerseNumber = (): number => {
      return this.verseNumber;
   }

   getGlobalVerseNumber = (): number => {
      return this.globalVerseNumber;
   }

   getText = (): string => {
      return this.text;
   }

   // Methods
   /**
    * Returns the bible verse in: "bookName, chapter, verseNumber" format.
    */
   getVerseIdentifier(): string {
      return this.bookName + ", " + this.chapter + ":" + this.verseNumber
   }
   /**
    * @param other 
    * @returns The distance in verse between this verse and another via globalVerseNumber.
    */
   getDistance(other: Verse): number {
      return Math.abs(this.globalVerseNumber - other.globalVerseNumber)
   }
}