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
    await fetch(SERVER_URL+"/bible/get-bible-data").then((response) => response.json()).then((data) => {
        try {
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
    const urlParams: Record<string, string> = {
        version: bibleVersion,
        // Convert to string.
        numOfContextVerses: requestedContext+""
    }
    await fetch(SERVER_URL+"/bible/random-verse?"+new URLSearchParams(urlParams).toString().replace("+","%20")).then((response => response.json())).then((data) => {
        try {
            return;
        } catch (e) {
            console.error(e)
            return;
        }
    })
    return;
}