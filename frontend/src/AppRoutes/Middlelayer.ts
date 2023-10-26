import { BibleData } from "../DataStructures/Global/BibleData";
import { Verse } from "../DataStructures/Global/Verse";
import { VerseGameSegment } from "../DataStructures/VerseGuesserGame/VerseGameSegment";

const SERVER_URL = "http://localhost:8888"

/**
 * Retrieves the bible data off of the server, including all bible versions and their chapter names.
 * 
 * @returns A promise for a BibleData object or undefined if the server is offline.
 */
export const getServerBibleData = async (): Promise<BibleData | undefined> => {
   console.log("Middlelayer | getServerBibleData")
   await fetch(SERVER_URL + "/bible/get-bible-data", {
      headers: {
         "Access-Control-Allow-Origin": "no-cors"
      }
   }).then((response) => {
      console.log(response)
      return response.json()
   }).then((data) => {
      try {
         console.log("Middlelayer | getServerBibleData | Retrieved: " + data)
         return new BibleData(
            data.bibleNames as string[],
            // Converts a Record<string, string[]> to a Map<string, string[]>
            Object.entries(data.bibleBookNames as Record<string, string[]>).reduce((accum: Map<string, string[]>, entry: [string, string[]]) => {
               return accum.set(entry[0], entry[1]), accum;
            }, new Map<string, string[]>()),
            data.dataMatrix as number[][]
         )
      } catch (e) {
         console.error(e)
         return;
      }
   }).catch((error) => {
      console.log("Middlelayer | getServerBibleData | Failed to retrieve data")
      console.error(error)
   })
   return;
}


/**
 * Note: The context is not guranteed. If you request 5 context, you expect 11 verses back. However, if the random verse is at the start of the book (genesis: 1, 1), then
 * it will return genesis chapter 1 as the verse to guess and 10 verse below and zero verses above.
 * 
 * @param bibleVersion The version of the bible according to the getServerBibleStandards
 * @param requestedContext The number of verses above and below the verse you requested. 
 * @returns 
 */
export const getRandomVerseSegment = async (bibleVersion: string, requestedContext: number): Promise<VerseGameSegment | undefined> => {
   console.log("Middlelayer | getRandomVerseSegment")
   const urlParams: Record<string, string> = {
      version: bibleVersion,
      // Convert to string.
      numOfContextVerses: requestedContext + ""
   }
   console.log("Middlelayer | getRandomVerseSegment | URL Params: " + urlParams.toString())
   await fetch(SERVER_URL + "/bible/random-verse?" + new URLSearchParams(urlParams).toString().replace("+", "%20")).then((response => response.json())).then((data) => {
      try {
         return;
      } catch (e) {
         console.error(e)
         return;
      }
   })
   return;
}