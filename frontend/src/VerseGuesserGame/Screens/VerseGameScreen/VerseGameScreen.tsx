import { useState, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import "./VerseGameScreen.css"
import { Verse } from "../../../DataStructures/Global/Verse"
import { BibleData } from "../../../DataStructures/Global/BibleData"

export interface VerseGameScreenProps {
   activeUserGameSegment: VerseGameSegment
   bibleData: BibleData
}

export const VerseGameScreen: React.FC<VerseGameScreenProps> = (props) => {
   const version = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getVersion)
   const verseBody = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getVerseBody)
   const verseToGuess = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getVerseToGuess)
   const allowedSurroundingVerses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getAllowedSurroundingVerses)
   const guessesCoiunt = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getGuessesCount)
   const previousUserGuesses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getPreviousUserGuesses)
   
   const [selectedBook, setSelectedBook] = useState(props.bibleData.bibleNames[0])
   const [selectedChapter, setSelectedChapter] = useState(1)
   const [selectedVerse, setSelectedVerse] = useState(1)

   const currentBookIndex = (props.bibleData.bibleBookNames.get(version) as string[]).indexOf(selectedBook)
   const chaptersInBook = props.bibleData.dataMatrix[currentBookIndex].length
   const versesInChapter = props.bibleData.dataMatrix[currentBookIndex][selectedChapter]

   return (
      <div className="VerseGameScreen-container">
         <h2>Guess the verse</h2>
         <p>{version}</p>
         {verseBody.map((currentVerse: Verse) => {
            return verseToGuess.text === currentVerse.text ? <p><b>{currentVerse.text}</b></p> : <p>{currentVerse.text}</p>
         })}
         <div className="VerseGameScreen">
            <p>Book Name</p>
            <select>
               {props.bibleData.bibleNames.map((name) => {
                  return <select>{name}</select>
               })}
            </select>
            <p>Chapter Number</p>
            <input min={0} max={chaptersInBook} value={selectedChapter} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
               // setSelectedVerse(1)
               setSelectedChapter(event.target.value as unknown as number)
            }} />
            <p>Current Verse</p>
            <input min={0} max={versesInChapter} value={selectedVerse} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
               setSelectedVerse(event.target.value as unknown as number)
            }} />
         </div>
      </div>
   )
}