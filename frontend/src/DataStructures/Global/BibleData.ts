export class BibleData {
   /**
    * List of bible version names
    */
   bibleNames: string[] = []
   /**
    * Maps a bible version name to the names of its books.
    */
   bibleBookNames: Map<string, string[]> = new Map<string, string[]>();
   /**
    * Each row is the book number (0 to 65) and each column index is the chapter number (0 to n).
    * Each cell is the amount of verses in that chapter of that book.
    * 
    * dataMatrix[bookIndex][chapterIndex]
    * 
    * @example dataMatrix[59][10] // Returns the amount of verses for book 60, chapter 11 of the bible.
    * 
    * 
    */
   dataMatrix: number[][] = [];
   constructor(bibleNames: string[], bibleBookNames: Map<string, string[]>, dataMatrix: number[][]) {
      this.bibleNames = bibleNames
      this.dataMatrix = dataMatrix;
      this.bibleBookNames = bibleBookNames;
   }
   /**
    * @param bibleVersion The bible version, case sensitive  
    * @param bookName The book in the bible, case sensitive
    * @returns The index, on a scale of 0 to 65 inclusive of this book in the bible.
    * 
    * @throws An error if the bible version or book name is not found
    */
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
   /**
    * @param bibleVersion The bible version, case sensitive  
    * @returns The list of book names for this bible in a 66 length list.
    */
   getBookNamesInVersion = (bibleVersion: string): string[] => {
      return this.bibleBookNames.get(bibleVersion) as string[]
   }
   /**
    * @param bibleVersion The bible version, case sensitive  
    * @param bookName The book in the bible, case sensitive
    * @returns The chapter count, on a scale of 1 to n, of the amount of chapters in the given book.
    */
   getChapterCountForBook = (bibleVersion: string, bookName: string): number => {
      return this.dataMatrix[(this.bibleBookNames.get(bibleVersion) as string[])?.indexOf(bookName)].length
   }
   /**
    * NOTE: Chapter number is an index.
    * 
    * @param bibleVersion The bible version, case sensitive  
    * @param bookName The book in the bible, case sensitive 
    * @param chapter The chapter number index (starting at zero). This is actual chapter minus one.
    * @returns The verse count of that chapter, starting at 1 (non-index)
    */
   getVerseCountForChapter = (bibleVersion: string, bookName: string, chapter: number) => {
      let result = this.dataMatrix[(this.bibleBookNames.get(bibleVersion) as string[])?.indexOf(bookName)][chapter]
      if (result === undefined) {
         console.error("BibleData | getVerseCountForChapter | Unable to find count for: "+bookName+" chapter "+chapter)
         return 1;
      }
      return this.dataMatrix[(this.bibleBookNames.get(bibleVersion) as string[])?.indexOf(bookName)][chapter]
   }
}