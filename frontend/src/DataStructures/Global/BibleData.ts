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
   constructor (bibleNames: string[], bibleBookNames: Map<string, string[]>, dataMatrix: number[][]) {
      this.bibleNames = bibleNames
      this.dataMatrix = dataMatrix;
      this.bibleBookNames = bibleBookNames;
   }
}