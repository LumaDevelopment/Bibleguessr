export class BibleData {
   /**
    * List of bible version names
    */
   bibleNames: string[] = []
   /**
    * Maps a bible version name to the names of its books.
    */
   bibleBookNames: Map<string, string[]> = new Map<string, string[]>();
   dataMatrix: number[][] = [];
   constructor(bibleNames: string[], bibleBookNames: Map<string, string[]>, dataMatrix: number[][]) {
      this.bibleNames = bibleNames
      this.dataMatrix = dataMatrix;
      this.bibleBookNames = bibleBookNames;
   }
   getBookIndex(bibleVersion: string, bookName: string): number {
      if (!this.bibleBookNames.has(bibleVersion)) {
         console.error("BibleData | getBookIndex | Unknown BibleVersion: " + bibleVersion)
         return -1;
      } else if (this.bibleBookNames.has(bibleVersion) && !(this.bibleBookNames.get(bibleVersion) as string[]).includes(bookName)) {
         console.error("BibleData | getBookIndex | Unknown BookName for bible version " + bibleVersion + " with book name: " + bookName)
         return -1;
      } else {
         return (this.bibleBookNames.get(bibleVersion) as string[]).indexOf(bookName)
      }
   }
   getBookNamesInVersion = (bibleVersion: string): string[] => {
      return this.bibleBookNames.get(bibleVersion) as string[]
   }
   getChapterCountForBook = (bibleVersion: string, bookName: string): number => {
      return this.dataMatrix[(this.bibleBookNames.get(bibleVersion) as string[])?.indexOf(bookName)].length
   }
   /**
    * 
    * @param bibleVersion 
    * @param bookName 
    * @param chapter The chapter number index. Note that this is an index, where the first chapter number is zero and not one.
    * @returns 
    */
   getVerseCountForChapter = (bibleVersion: string, bookName: string, chapter: number) => {
      return this.dataMatrix[(this.bibleBookNames.get(bibleVersion) as string[])?.indexOf(bookName)][chapter]
   }
}