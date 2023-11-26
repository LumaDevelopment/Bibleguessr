import { useEffect, useState, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import "./VerseGameScreen.css"
import { Verse } from "../../../DataStructures/Global/Verse"
import { BibleData } from "../../../DataStructures/Global/BibleData"
import { VerseGameStore } from "../../VerseGameManager/VerseGameStore"
import { getRandomVerseGameSegment } from "../../../AppRoutes/Middlelayer"

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

   const errorLoadingSegment: boolean = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getErrorLoadingVerses)
   const isLoadingVerses: boolean = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getIsLoadingVerses)

   const [bookGuess, setBookGuess] = useState((bibleData.bibleBookNames.get(bibleVersion) as string[])[0] as string)
   const [chapterGuess, setChapterGuess] = useState(1)
   const [verseNumberGuess, setVerseNumberGuess] = useState(1)

   console.log(activeGameSegment)

   if (bibleData === undefined || verseToGuess === undefined) {
      return (
         <div className="VerseGameScreen-container">
            <div className="VerseGameScreen-loading">
               <h2>Guess the verse</h2>
               <p>Loading Bible Data...</p>
            </div>
         </div>
      )
   }

   if (isLoadingVerses) {
      return (<div className="VerseGameScreen-container">
         <div className="VerseGameScreen-loading">
            <h2>Guess the verse</h2>
            <p>Loading verse...</p>
         </div>
      </div>)
   }

   if (errorLoadingSegment) {
      return (<div className="VerseGameScreen-container">
         <div className="VerseGameScreen-loading">
            <h2>Guess the verse</h2>
            <p>Unable to load verse</p>
         </div>
      </div>)
   }

   return (
      <div className="VerseGameScreen-container">
         <h2>Guess the verse</h2>
         <p>{bibleVersion}</p>
         <p>{guesses}</p>
         <p>{verseToGuess.bookName}, {verseToGuess.chapter}, {verseToGuess.verseNumber}</p>
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
                  setChapterGuess(Number(event.target.value))
               }} />
            </div>
            <div>
               <p>Verse</p>
               <input min={1} max={bibleData.getVerseCountForChapter(bibleVersion, bookGuess, chapterGuess)} value={verseNumberGuess} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                  setVerseNumberGuess(Number(event.target.value))
               }} />
            </div>
         </div>
         <a className="VerseGameScreen-guess-button" onClick={() => {
            let correct = bookGuess === verseToGuess.bookName && chapterGuess === verseToGuess.chapter && verseNumberGuess === verseToGuess.verseNumber
            let verseUserGuessed = new Verse(bibleVersion, bookGuess, chapterGuess, verseNumberGuess, -1, "");
            activeGameSegment.addPreviousGuess(verseUserGuessed);
            if (correct) {
               let nextSegment: VerseGameSegment = new VerseGameSegment(activeGameSegment.getBibleVersion(), activeGameSegment.getContextVersesDefault());
               nextSegment.initVerses();
               verseGameStore.addNewGameSegment(nextSegment);
            } else {
               console.log("Incorrect");
            }
         }}>
            Guess
         </a>
      </div>
   )
}