import { useEffect, useState, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import "./VerseGameScreen.css"
import { Verse } from "../../../DataStructures/Global/Verse"
import { BibleData } from "../../../DataStructures/Global/BibleData"
import { VerseGameStore } from "../../VerseGameStore"
import { VerseGuess } from "../../../DataStructures/Global/VerseGuess"

export interface VerseGameScreenProps {
   verseGameStore: VerseGameStore
}

export const VerseGameScreen: React.FC<VerseGameScreenProps> = (props) => {


   const { verseGameStore } = props

   const activeGameSegment: VerseGameSegment = useSyncExternalStore(verseGameStore.subscribe, verseGameStore.getActiveGameSegment)
   const bibleData = useSyncExternalStore(verseGameStore.subscribe, verseGameStore.getBibleData)
   const bibleVersion = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getBibleVersion)
   const contextVersesAbove = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getContextVersesAbove)
   const contextVersesBelow = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getContextVersesBelow)
   const verseToGuess = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getVerseToGuess)
   const guesses = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getGuesses)

   const [bookGuess, setBookGuess] = useState((bibleData.bibleBookNames.get(bibleVersion) as string[])[0] as string)
   const [chapterGuess, setChapterGuess] = useState(1)
   const [verseNumberGuess, setVerseNumberGuess] = useState(1)

   useEffect(() => {
      activeGameSegment.initVerses();
   }, [])

   console.log(activeGameSegment)

   if (bibleData === undefined || verseToGuess == undefined) {
      return (
         <div className="VerseGameScreen-container">
            <div className="VerseGameScreen-loading">
               <h2>Guess the verse</h2>
               <p>Loading...</p>
            </div>
         </div>
      )
   }

   return (
      <div className="VerseGameScreen-container">
         <h2>Guess the verse</h2>
         <p>{bibleVersion}</p>
         <p>{guesses}</p>
         <p>{contextVersesBelow.map((currentVerse: Verse) => currentVerse.text + " ")}<b>{verseToGuess.text + " "}</b>{contextVersesAbove.map((currentVerse: Verse) => currentVerse.text + " ")}</p>
         <div className="VerseGameScreen-guessing">
            <div>
               <p>Book</p>
               <select onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                  setBookGuess(event.target.value)
               }}>
                  {bibleData.bibleBookNames.get(bibleVersion)?.map((name: string, i: number) => <option key={"VerseGameScreen_bible_version_option_" + i}>{name}</option>)}
               </select>
            </div>
            <div>
               <p>Chapter</p>
               <input min={1} max={bibleData.getChapterCountForBook(bibleVersion, bookGuess)} value={chapterGuess} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                  setChapterGuess(event.target.value as unknown as number)
               }} />
            </div>
            <div>
               <p>Verse</p>
               <input min={1} max={bibleData.getVerseCountForChapter(bibleVersion, bookGuess, chapterGuess)} value={verseNumberGuess} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                  setVerseNumberGuess(event.target.value as unknown as number)
               }} />
            </div>
         </div>
         <a className="VerseGameScreen-guess-button" onClick={() => {
            console.log("Hello")
            let correct = bookGuess === verseToGuess.bookName && chapterGuess === verseToGuess.chapter && verseNumberGuess === verseToGuess.verseNumber
            let verseUserGuessed = new VerseGuess(bibleVersion, bookGuess, chapterGuess, verseNumberGuess, correct);
            activeGameSegment.addPreviousGuess(verseUserGuessed);
            if (correct) {
               // Do stuff
            }
         }}>
            Guess
         </a>
      </div>
   )
}