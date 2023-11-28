import { useEffect, useMemo, useState, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import "./VerseGameScreen.css"
import { Verse } from "../../../DataStructures/Global/Verse"
import { BibleData } from "../../../DataStructures/Global/BibleData"
import { VerseGameStore } from "../../VerseGameManager/VerseGameStore"
import { getRandomVerseGameSegment, setGlobalIndexFromVerse } from "../../../AppRoutes/Middlelayer"
import "../../../DataStructures/Global/Buttons.css"
import { useNavigate, useNavigation } from "react-router-dom"

interface VerseGameScreenVerseTextProps {
   verse: Verse
   verseHasBeenGuessed: boolean
   shouldBeBolded: boolean
}

// Hold a "If verse is on screen and it has been guessed boolean array"
const VerseGameScreenVerseText: React.FC<VerseGameScreenVerseTextProps> = (props) => {
   const { verse, verseHasBeenGuessed, shouldBeBolded } = props;
   const text = useSyncExternalStore(verse.subscribe, verse.getText)
   console.log("VerseGameScreenVerseText | Rendering with guessed status " + verseHasBeenGuessed)
   return (
      <p style={{
         "border": (verseHasBeenGuessed ? "1px solid red" : ""),
         "borderRadius": "10px",
         "fontWeight": (shouldBeBolded ? "600" : "400"),
      }}
         key={"VerseGameScreen_Verse_" + verse.getGlobalVerseNumber()}
         id={text.split(" ").join("-")}
      >
         {!verseHasBeenGuessed ? <b>✞ </b> : <b>{verse.getVerseNumber()} </b>}
         {text}
      </p>
   )
}

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
   const isProcessingUserGuess = useSyncExternalStore(verseGameStore.subscribe, verseGameStore.getIsProcessingUserGuess)

   const errorLoadingSegment: boolean = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getErrorLoadingVerses)
   const isLoadingVerses: boolean = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getIsLoadingVerses)

   const [bookGuess, setBookGuess] = useState((bibleData.bibleBookNames.get(bibleVersion) as string[])[0] as string)
   const [chapterGuess, setChapterGuess] = useState(1)
   const [verseNumberGuess, setVerseNumberGuess] = useState(1)

   const hasUserAlreadyGuessedThisVerse: boolean = useMemo(() => {
      if (guesses === 0) {
         return false;
      }
      for (let previousVerse of activeGameSegment.getPreviousGuesses()) {
         if (previousVerse.getBookName() === bookGuess &&
            previousVerse.getChapter() === chapterGuess &&
            previousVerse.getVerseNumber() === verseNumberGuess
         ) {
            return true;
         }
      }
      return false;
   }, [bookGuess, chapterGuess, verseNumberGuess, guesses])

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

   // const navigator = useNavigate()

   return (
      <div className="VerseGameScreen-container" style={isProcessingUserGuess ? { "cursor": "wait" } : {}}>
         <h2>Guess the bolded verse</h2>
         <h3>Round {verseGameStore.getGameSegments().length + 1}</h3>
         {/* <p>{verseToGuess.getBookName()}, {verseToGuess.getChapter()}, {verseToGuess.getVerseNumber()}</p> */}
         <div className="Block-button-wrapper">
            <button className="Block-button Block-button-blue Block-button-extended" onClick={() => {
               // navigator(verseToGuess.getText().split(" ").join("-"))
            }}>Jump To Verse</button>
         </div>
         <div className="VerseGameScreen-text-container">
            <p className="VerseGameScreen-bibleVersion"><b>{bibleVersion}📖</b></p>
            {contextVersesBelow.map((currentVerse: Verse) => <VerseGameScreenVerseText key={"VerseGameScreen-context-verse-below-" + currentVerse.getGlobalVerseNumber()} verse={currentVerse} verseHasBeenGuessed={activeGameSegment.previousGuessesContainsVerse(currentVerse)} shouldBeBolded={false} />)}
            <VerseGameScreenVerseText verse={verseToGuess} verseHasBeenGuessed={false} shouldBeBolded={true} />
            {contextVersesAbove.map((currentVerse: Verse) => <VerseGameScreenVerseText key={"VerseGameScreen-context-verse-above-" + currentVerse.getGlobalVerseNumber()} verse={currentVerse} verseHasBeenGuessed={activeGameSegment.previousGuessesContainsVerse(currentVerse)} shouldBeBolded={false} />)}
         </div>
         <div className="VerseGameScreen-guessing">
            <div>
               <p>Book</p>
               <select className="VerseGameScreen-select" onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {
                  setBookGuess(event.target.value)
               }}>
                  {bibleData.bibleBookNames.get(bibleVersion)?.map((name: string, i: number) => <option key={"VerseGameScreen_bible_version_option_" + i}>{name}</option>)}
               </select>
            </div>
            <div>
               <p>Chapter</p>
               <input className="VerseGameScreen-number" min={1} max={bibleData.getChapterCountForBook(bibleVersion, bookGuess)} value={chapterGuess} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                  setChapterGuess(Number(event.target.value))
               }} />
            </div>
            <div>
               <p>Verse</p>
               <input className="VerseGameScreen-number" min={1} max={bibleData.getVerseCountForChapter(bibleVersion, bookGuess, chapterGuess)} value={verseNumberGuess} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                  setVerseNumberGuess(Number(event.target.value))
               }} />
            </div>
         </div>
         <div className="Block-button-wrapper">
            <button
               className={"Block-button Block-button-yellow"}
               onClick={async () => {
                  if (isProcessingUserGuess || hasUserAlreadyGuessedThisVerse) {
                     return;
                  }
                  verseGameStore.setIsProcessingUserGuess(true)
                  let nextSegment: VerseGameSegment = new VerseGameSegment(activeGameSegment.getBibleVersion(), activeGameSegment.getContextVersesDefault());
                  nextSegment.initVerses()
                  verseGameStore.addNewGameSegment(nextSegment);
                  verseGameStore.setIsProcessingUserGuess(false)
               }}>
               Skip
            </button>
            <button
               className={"Block-button" + (hasUserAlreadyGuessedThisVerse ? " Block-button-red" : " Block-button-green")}
               onClick={async () => {
                  if (isProcessingUserGuess || hasUserAlreadyGuessedThisVerse) {
                     return;
                  }
                  verseGameStore.setIsProcessingUserGuess(true)
                  let correct = bookGuess === verseToGuess.getBookName() && chapterGuess === verseToGuess.getChapter() && verseNumberGuess === verseToGuess.getVerseNumber()
                  let verseUserGuessed = new Verse(bibleVersion, bookGuess, chapterGuess, verseNumberGuess, -1, "");
                  await setGlobalIndexFromVerse(verseUserGuessed, bibleData)
                  activeGameSegment.addPreviousGuess(verseUserGuessed);
                  if (correct) {
                     activeGameSegment.setHasSuccessfullyGuessed(true)
                     let nextSegment: VerseGameSegment = new VerseGameSegment(activeGameSegment.getBibleVersion(), activeGameSegment.getContextVersesDefault());
                     nextSegment.initVerses()
                     verseGameStore.addNewGameSegment(nextSegment);
                  } else {
                     console.log("VerseGameScreen | Incorrect");
                  }
                  verseGameStore.setIsProcessingUserGuess(false)
               }}>
               Guess {guesses + 1}
            </button>
         </div>
         {guesses > 0 && <div className="VerseGameScreen-previous-guesses">
            <h4>Previous Guesses</h4>
            {activeGameSegment.getPreviousGuesses().map((value: Verse, index: number) => {
               var currentlySelected: boolean = false;
               if (
                  value.getBookName() === bookGuess &&
                  value.getChapter() === chapterGuess &&
                  value.getVerseNumber() === verseNumberGuess
               ) {
                  currentlySelected = true;
               }
               return <p key={"VerseGameScreen-previousGuesses-" + index}> {index + 1}) <span className={currentlySelected ? "VerseGameScreen-previous-guesses-selected" : ""}>{value.getVerseIdentifier()}</span></p>
            })}
         </div>}
      </div>
   )
}