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
   getBookNamesInVersion = (bibleVersion: string): string[]  => {
      return this.bibleBookNames.get(bibleVersion) as string[]
   }
   getChapterCountForBook = (bibleVersion: string, bookName: string): number => {
      return this.dataMatrix[(this.bibleBookNames.get(bibleVersion) as string[])?.indexOf(bookName)].length   
   }
   getVerseCountForChapter = (bibleVersion: string, bookName: string, chapter: number) => {
      return this.dataMatrix[(this.bibleBookNames.get(bibleVersion) as string[])?.indexOf(bookName)][chapter]
   }
}