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
   return fetch(SERVER_URL + "/bible/get-bible-data").then((response) => response.json()).then((data) => {
      try {
         console.log("Middlelayer | getServerBibleData | Retrieved data!")
         return new BibleData(
            data.bibleNames as string[],
            Object.entries(data.bibleBookNames as Record<string, string[]>).reduce((accum: Map<string, string[]>, entry: [string, string[]]) => {
               return accum.set(entry[0], entry[1]), accum;
            }, new Map<string, string[]>()),
            data.dataMatrix as number[][]
         )
         /**
          * Error in TypeScript are not known until run time, so the type can be any (Not sure how that makes sense)
          * https://byby.dev/ts-try-catch-error-type
          * Therefor, I need to check if the error... is an error before printing its message.
          */
      } catch (e) {
         if (e instanceof Error) {
            console.error("Middlelayer | getServerBibleData | Parsing Error: " + e.message);
         } else {
            console.error("Middlelayer | getServerBibleData | Parsing Error: " + e);
         }
         return undefined;
      }
   }).catch((e) => {
      if (e instanceof Error) {
         console.error("Middlelayer | getServerBibleData | Retrieval Error: " + e.message);
      } else {
         console.error("Middlelayer | getServerBibleData | Retrieval Error: " + e);
      }
      return undefined;
   })
}


/**
 * Note: The context is not guranteed. If you request 5 context, you expect 11 verses back. However, if the random verse is at the start of the book (genesis: 1, 1), then
 * it will return genesis chapter 1 as the verse to guess and 10 verse below and zero verses above.
 * 
 * @returns 
 */
export const getRandomVerseGameSegment = async (currentSegment: VerseGameSegment): Promise<VerseGameSegment | undefined> => {
   console.log("Middlelayer | getRandomVerse")
   currentSegment.setIsLoadingVerses(true);
   currentSegment.setErrorLoadingVerses(false);
   const urlParams: Record<string, string> = {
      version: currentSegment.getBibleVersion(),
      // Convert to string.
      numOfContextVerses: currentSegment.getContextVersesDefault() + ""
   }
   await fetch(SERVER_URL + "/bible/random-verse?" + new URLSearchParams(urlParams).toString().replace("+", "%20"), {
      headers: {
         "Access-Control-Allow-Origin": "no-cors"
      }
   }).then((response => response.json())).then((data) => {
      // Process the data here
      try {
         const belowVerses: Verse[] = []
         const aboveVerses: Verse[] = []
         for (let i = 0; i < data.verseArray.length; i++) {
            const verseData = data.verseArray[i];
            const verseObject: Verse = new Verse(
               data.bibleVersion,
               verseData.book,
               verseData.chapter,
               verseData.verse,
               verseData.universalIndex,
               verseData.text
            )
            if (i < data.localVerseIndex) {
               belowVerses.push(verseObject)
            } else if (i > data.localVerseIndex) {
               aboveVerses.push(verseObject)
            } else {
               currentSegment.setVerseToGuess(verseObject)
            }
         }
         currentSegment.setContextVersesAbove(aboveVerses)
         currentSegment.setContextVersesBelow(belowVerses)
         currentSegment.setIsLoadingVerses(false);
         currentSegment.setErrorLoadingVerses(false);
         return currentSegment;
      } catch (e) {
         if (e instanceof Error) {
            console.error("Middlelayer | getRandomVerse | Parsing Error: " + e.message);
         } else {
            console.error("Middlelayer | getRandomVerse | Parsing Error: " + e);
         }
         currentSegment.setIsLoadingVerses(false);
         currentSegment.setErrorLoadingVerses(true);
         return undefined;
      }
   })
   // This code hypothetically should never be reached.
   currentSegment.setIsLoadingVerses(false);
   currentSegment.setErrorLoadingVerses(false);
   return currentSegment;
}