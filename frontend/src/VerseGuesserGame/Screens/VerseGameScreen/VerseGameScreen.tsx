import { useMemo, useRef, useState, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import "./VerseGameScreen.css"
import { Verse } from "../../../DataStructures/Global/Verse"
import { VerseGameStore } from "../../VerseGameManager/VerseGameStore"
import { increaseCount, setGlobalIndexFromVerse } from "../../../AppRoutes/Middlelayer"
import "../../../DataStructures/Global/Buttons.css"

interface VerseGameScreenVerseTextProps {
   verse: Verse
   verseHasBeenGuessed: boolean
   shouldBeBolded: boolean
   /**
    * Only the verse to guess needs a REF, which allows for the jump to verse function to work.
    */
   innerRef: React.RefObject<HTMLParagraphElement> | undefined
}

/**
 * Generates the UI for a single verse on the screen.
 * 
 * @param props 
 * @returns 
 */
const VerseGameScreenVerseText: React.FC<VerseGameScreenVerseTextProps> = (props) => {
   const { verse, verseHasBeenGuessed, shouldBeBolded } = props;
   const text = useSyncExternalStore(verse.subscribe, verse.getText)
   return (
      <p style={{
         "border": (verseHasBeenGuessed ? "1px solid red" : shouldBeBolded ? "1px solid black" : ""),
         "borderRadius": "5px",
         "fontWeight": (shouldBeBolded ? "600" : "400"),
         "boxShadow": shouldBeBolded ? "1px 1px 10px black" : ""
      }}
         ref={props.innerRef}
         key={"VerseGameScreen_Verse_" + verse.getGlobalVerseNumber()}
         id={text.split(" ").join("-")}>
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

   // Subscribes to the central verse game store to get the active game segment

   const activeGameSegment: VerseGameSegment = useSyncExternalStore(verseGameStore.subscribe, verseGameStore.getActiveGameSegment)
   const hintCount = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getHints)
   const bibleData = useSyncExternalStore(verseGameStore.subscribe, verseGameStore.getBibleData)
   const bibleVersion = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getBibleVersion)
   const contextVersesAbove = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getContextVersesAbove)
   const contextVersesBelow = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getContextVersesBelow)
   const verseToGuess = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getVerseToGuess)
   const guesses = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getGuesses)
   const isProcessingUserGuess = useSyncExternalStore(verseGameStore.subscribe, verseGameStore.getIsProcessingUserGuess)
   const errorLoadingSegment: boolean = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getErrorLoadingVerses)
   const isLoadingVerses: boolean = useSyncExternalStore(activeGameSegment.subscribe, activeGameSegment.getIsLoadingVerses)

   // These are the internal states for this component

   const [bookGuess, setBookGuess] = useState((bibleData.bibleBookNames.get(bibleVersion) as string[])[0] as string)
   const [chapterGuess, setChapterGuess] = useState(1)
   const [verseNumberGuess, setVerseNumberGuess] = useState(1)
   
   // The max values for the input boxes.

   const [maxVerseCount, setMaxVerseCount] = useState(bibleData.getVerseCountForChapter(bibleVersion, bookGuess, chapterGuess))
   const [maxChapterCount, setMaxChapterCount] = useState(bibleData.getChapterCountForBook(bibleVersion, bookGuess))

   /**
    * Stores the component reference for the verse to guess so the "jump to verse" button can navigate to it.
    */
   const verseRef = useRef<HTMLParagraphElement>(null)

   /**
    * Checks if the current user verse that the user has inputted is already been guessed,
    * preventing them from guessing the same verse multiple times.
    * Memorizes the result between component re-renders unless values of the guess states have changed
    */
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

   // Here is the start of the component rendering.
   // It's important to note that the return statements must be the last item in the function.
   // No hook can be done between return statement declaration.

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

   // For example, here I could not put useMemo, a useState or any other hook code.
   // This is causes a fatal differing hook count rendering error.

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

   // Since these values are not hook calls, and are re-computed on every re-render of the component,
   // They are allowed to be between the component return statements.

   const shouldDisplayHint1 = (hintCount >= 1) && (bookGuess !== verseToGuess.getBookName())
   const shouldDisplayHint2 = (hintCount >= 2) && (chapterGuess !== verseToGuess.getChapter())
   const shouldDisplayHint3 = (hintCount >= 3) && (verseNumberGuess !== verseToGuess.getVerseNumber())

   return (
      <div className="VerseGameScreen-container" style={isProcessingUserGuess ? { "cursor": "wait" } : {}}>
         <h2>Guess the bolded verse</h2>
         <h2>Round {verseGameStore.getGameSegments().length + 1}</h2>
         {/* <p>{verseToGuess.getBookName()}, {verseToGuess.getChapter()}, {verseToGuess.getVerseNumber()}</p> */}
         <div className="Block-button-wrapper">
            <button className="Block-button Block-button-blue Block-button-extended" onClick={() => {
               if (verseRef.current) {
                  // Make sure to use .current to access the html object from a ref object.
                  verseRef.current.scrollIntoView({ behavior: 'smooth', block: "center" })
               }
            }}>Jump To Verse</button>
         </div>
         <div className="VerseGameScreen-text-container">
            <p className="VerseGameScreen-bibleVersion"><b>{bibleVersion}📖</b></p>
            {contextVersesBelow.map((currentVerse: Verse) => <VerseGameScreenVerseText innerRef={undefined} key={"VerseGameScreen-context-verse-below-" + currentVerse.getGlobalVerseNumber()} verse={currentVerse} verseHasBeenGuessed={activeGameSegment.previousGuessesContainsVerse(currentVerse)} shouldBeBolded={false} />)}
            <VerseGameScreenVerseText innerRef={verseRef} verse={verseToGuess} verseHasBeenGuessed={false} shouldBeBolded={true} />
            {contextVersesAbove.map((currentVerse: Verse) => <VerseGameScreenVerseText innerRef={undefined} key={"VerseGameScreen-context-verse-above-" + currentVerse.getGlobalVerseNumber()} verse={currentVerse} verseHasBeenGuessed={activeGameSegment.previousGuessesContainsVerse(currentVerse)} shouldBeBolded={false} />)}
         </div>
         <div className="VerseGameScreen-guessing">
            <div>
               <p>Book</p>
               <select
                  className={"VerseGameScreen-select " + (shouldDisplayHint1 ? "VerseGameScreen-select-border" : "")}
                  onChange={(event: React.ChangeEvent<HTMLSelectElement>) => {

                     // Re-calculates the max chapters and make verses for the input fields whenever the book changes
                     // Since different books have different chapter and verse counts.

                     const maxChapters =  bibleData.getChapterCountForBook(bibleVersion, event.target.value)
                     if (maxChapters === undefined) {
                        console.error("VerseGameScreen | Book Guess | Unable to find max chapters for: "+bibleVersion+", "+event.target.value)
                     }
                     setMaxChapterCount(maxChapters)
                     // Resets the chapter guess if it exceeds the max from the re-calculation.
                     if (chapterGuess > maxChapters) {
                        setChapterGuess(maxChapters)
                     }
                     const maxVerses = bibleData.getVerseCountForChapter(bibleVersion, event.target.value, maxChapters-1)
                     if (maxVerses === undefined) {
                        console.error("VerseGameScreen | Book Guess | Unable to find max verses for: "+bibleVersion+", "+event.target.value+", and chapter: "+maxChapters)
                     }
                     setMaxVerseCount(maxVerses)
                     if (verseNumberGuess > maxVerses) {
                        setVerseNumberGuess(maxVerses)
                     }
                     setBookGuess(event.target.value)
                  }}>
                  {bibleData.bibleBookNames.get(bibleVersion)?.map((name: string, i: number) => <option key={"VerseGameScreen_bible_version_option_" + i}>{name}</option>)}
               </select>
            </div>
            <div>
               <p>Chapter</p>
               <input
                  type="number"
                  className={"VerseGameScreen-number " + (shouldDisplayHint2 ? "VerseGameScreen-select-border" : "")}
                  min={1}
                  max={maxChapterCount} value={chapterGuess} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                     // Only re-calculates the max verses whenever the chapter changes.
                     const maxVerses = bibleData.getVerseCountForChapter(bibleVersion, bookGuess, Number(event.target.value)-1)
                     if (maxVerses === undefined) {
                        console.error("VerseGameScreen | Book Guess | Unable to find max verses for: "+bibleVersion+", "+event.target.value+", and chapter: "+maxChapterCount)
                     }
                     setMaxVerseCount(maxVerses)
                     if (verseNumberGuess > maxVerses) {
                        setVerseNumberGuess(maxVerses)
                     }
                     setChapterGuess(Number(event.target.value))
                  }} />
            </div>
            <div>
               <p>Verse</p>
               <input
                  type="number"
                  className={"VerseGameScreen-number " + (shouldDisplayHint3 ? "VerseGameScreen-select-border" : "")}
                  min={1}
                  max={maxVerseCount} value={verseNumberGuess} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                     setVerseNumberGuess(Number(event.target.value))
                  }} />
            </div>
         </div>
         <div className="Block-button-wrapper">
            {hintCount < 3 && <button className="Block-button Block-button-orange" onClick={() => activeGameSegment.addHint()}>
               Hint
            </button>}
            <button
               className={"Block-button " + (hintCount < 3 ? "Block-button-yellow" : "Block-button-green")}
               onClick={async () => {
                  if (isProcessingUserGuess) {
                     return;
                  }
                  verseGameStore.setIsProcessingUserGuess(true)
                  let nextSegment: VerseGameSegment = new VerseGameSegment(activeGameSegment.getBibleVersion(), activeGameSegment.getContextVersesDefault());
                  nextSegment.initVerses()
                  verseGameStore.addNewGameSegment(nextSegment);
                  verseGameStore.setIsProcessingUserGuess(false)
               }}>
               {hintCount < 3 ? "Skip" : "Next"}
            </button>
            {hintCount < 3 && <button
               className={"Block-button" + (hasUserAlreadyGuessedThisVerse ? " Block-button-red" : " Block-button-green")}
               onClick={async () => {
                  if (isProcessingUserGuess || hasUserAlreadyGuessedThisVerse) {
                     return;
                  }
                  increaseCount()
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
                     setBookGuess((bibleData.bibleBookNames.get(bibleVersion) as string[])[0] as string)
                     setChapterGuess(1)
                     setVerseNumberGuess(1)
                  } else {
                     console.log("VerseGameScreen | Incorrect");
                  }
                  verseGameStore.setIsProcessingUserGuess(false)
               }}>
               Guess {guesses + 1}
            </button>}
         </div>
         {hintCount > 0 && <div className="VerseGameScreen-hints">
            <p><b>Hints</b></p>
            {hintCount >= 1 && <p>Book: {verseToGuess.getBookName()}</p>}
            {hintCount >= 2 && <p>Chapter: {verseToGuess.getChapter()}</p>}
            {hintCount >= 3 && <p>Verse: {verseToGuess.getVerseNumber()}</p>}
         </div>}
         {guesses > 0 && <div className="VerseGameScreen-previous-guesses">
            <p><b>Previous Guesses</b></p>
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