import { BibleData } from "../DataStructures/Global/BibleData";
import { Verse } from "../DataStructures/Global/Verse";
import { VerseGameSegment } from "../DataStructures/VerseGuesserGame/VerseGameSegment";

const SERVER_URL = "https://api.bibleguessr.gg"

/**
 * Retrieves the bible data off of the server, including all bible versions and their chapter names.
 * 
 * @returns A promise for a BibleData object or undefined if the server is offline.
 */
export const getServerBibleData = async (): Promise<BibleData | undefined> => {
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
 * Inits a game segment with random verse data based on its context.
 * 
 * Note: The context is not guaranteed. If you request 5 context, you expect 11 verses back. However, if the random verse is at the start of the book (genesis: 1, 1), then
 * it will return genesis chapter 1 as the verse to guess and 10 verse below and zero verses above.
 * 
 * @returns 
 */
export const getRandomVerseGameSegment = async (currentSegment: VerseGameSegment): Promise<VerseGameSegment | undefined> => {
   console.log("Middlelayer | getRandomVerseGameSegment | Called")
   currentSegment.setIsLoadingVerses(true);
   currentSegment.setErrorLoadingVerses(false);
   const urlParams: Record<string, string> = {
      version: currentSegment.getBibleVersion(),
      // Convert to string.
      numOfContextVerses: currentSegment.getContextVersesDefault() + ""
   }
   return await fetch(SERVER_URL + "/bible/random-verse?" + new URLSearchParams(urlParams).toString().replace("+", "%20"), {
      headers: {
         "Access-Control-Allow-Origin": "no-cors"
      }
   }).then((response => response.json())).then((data) => {
      // Process the data here
      console.log("Middlelayer | getRandomVerseGameSegment | Received Response", data)
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
         console.log("Middlelayer | getRandomVerseGameSegment | Finished Processing Data", currentSegment)
         return currentSegment;
      } catch (e) {
         console.error("Middlelayer | getRandomVerse | Parsing Error: ", e);
         currentSegment.setIsLoadingVerses(false);
         currentSegment.setErrorLoadingVerses(true);
         return undefined;
      }
   })
}


/**
 * Sets the global index of a verse object. Sets it as -1 if it is not found.
 * 
 * @param verse 
 * @param bibleData 
 * @returns 
 */
export const setGlobalIndexFromVerse = async (verse: Verse, bibleData: BibleData): Promise<number> => {
   console.log("Middlelayer | setGlobalIndexFromVerse | Called For " + verse.getVerseIdentifier());
   const bookIndex = bibleData.getBookIndex(verse.getBibleVersion(), verse.getBookName())
   const URL = `${SERVER_URL}/bible/index-by-reference?bookIndex=${bookIndex}&chapterNum=${verse.getChapter()}&verseNum=${verse.getVerseNumber()}`
   return await fetch(URL, {
      headers: {
         "Access-Control-Allow-Origin": "no-cors"
      }
   }).then((response => response.json())).then((data) => {
      try {
         const index = Number(data.index)
         if (index === -1) {
            console.error("Middlelayer | getGlobalIndexFromVerse | Unable to find global index for verse");
         }
         console.log("Middlelayer | getGlobalIndexFromVerse | Found global index for verse: " + index + " w/ " + verse.getVerseIdentifier());
         verse.setGlobalVerseNumber(index);
         return index;
      } catch (e) {
         console.error("Middlelayer | getGlobalIndexFromVerse | Parsing Error: ", e);
         verse.setGlobalVerseNumber(-1)
         return -1;
      }
   });
}


/**
 * Gets the global verse count from the server.
 * 
 * @returns 
 */
export const getCount = async (): Promise<number | undefined> => {
   return await fetch(SERVER_URL + "/guess-counter/get-count", {
      headers: {
         "Access-Control-Allow-Origin": "no-cors"
      }
   }).then((response => response.json())).then((data) => {
      try {
         const num = data.count;
         console.log("Middlelayer | getCount | Fetched count data", num)
         return num
      } catch (e) {
         console.log("Middlelayer | getCount | Unable to process getCount data", e)
      }
   }).catch((e) => {
      console.error("Middlelayer | getCount | Unable to fetch count with error", e)
   })
}

/**
 * Increases the global verse count.
 */
export const increaseCount = async (): Promise<void> => {
   await fetch(SERVER_URL + "/guess-counter/increment-count", {
      headers: {
         "Access-Control-Allow-Origin": "no-cors"
      }
   }).catch((e) => {
      console.error("Middlelayer | increaseCount | Unable to increase count with error", e)
   })
}