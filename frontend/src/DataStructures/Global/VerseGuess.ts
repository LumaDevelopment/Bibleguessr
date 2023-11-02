export class VerseGuess {
   bibleVersion!: String;
   bookName!: String;
   chapter!: number;
   verseNumber!: number;
   correct: boolean = false
   constructor(bibleVersion: String, bookName: String, chapter: number, verseNumber: number, correct: boolean) {
       this.bibleVersion = bibleVersion;
       this.bookName = bookName;
       this.chapter = chapter;
       this.verseNumber = verseNumber;
       this.correct = correct;
   }
}