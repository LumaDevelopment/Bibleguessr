import { useSyncExternalStore } from "react"
import { VerseGameStore } from "../../VerseGameManager/VerseGameStore"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { Verse } from "../../../DataStructures/Global/Verse"
import "./FinishScreen.css"
interface SegmentBreakdown {
   gameSegment: VerseGameSegment
   roundIndex: number
}

/**
 * Provides a HTML view of a game segment.
 * 
 * Due to styling, this component is not modular and only is used for the FinishScreen component.
 * 
 * As a result, it is not exported outside of the FinishScreen file.
 * 
 * @param props 
 * @returns 
 */
const SegmentBreakdown: React.FC<SegmentBreakdown> = (props) => {
   console.log("SegmentBreakDown | Received", props)
   const verseToGuess: Verse = useSyncExternalStore(props.gameSegment.subscribe, props.gameSegment.getVerseToGuess) as Verse;
   const guesses = useSyncExternalStore(props.gameSegment.subscribe, props.gameSegment.getGuesses);
   const pastGuesses = useSyncExternalStore(props.gameSegment.subscribe, props.gameSegment.getPreviousGuesses)
   const hasSuccessfullyGuessed = useSyncExternalStore(props.gameSegment.subscribe, props.gameSegment.getHasSuccessfullyGuessed)
   const guessScores = useSyncExternalStore(props.gameSegment.subscribe, props.gameSegment.getGuessScore)
   const finalRoundScore = useSyncExternalStore(props.gameSegment.subscribe,props.gameSegment.getFinalRoundScore)
   const hintHistory = useSyncExternalStore(props.gameSegment.subscribe, props.gameSegment.getHintHistory)
   return (
      <div className="SegmentBreakdown-container"
         style={{ "backgroundColor": (props.roundIndex % 2 === 0 ? "#FFFFFF" : "#EFEFEF") }}>
         <h2 className="SegmentBreakdown-verse">{verseToGuess.getVerseIdentifier()}</h2>
         <p>Round {props.roundIndex + 1} Score: <b>{finalRoundScore}</b></p>
         <p>Context Verses: <b>{props.gameSegment.getContextVersesBelow().length + props.gameSegment.getContextVersesAbove().length}</b></p>
         {guesses > 1 && <div className="SegmentBreakdown-guess-list-wrapper"><p>Guesses:</p>
            <ol>
               {pastGuesses.map((value: Verse, index: number) => {
                  const hintText = hintHistory[index] === 0 ? "" : `with ${hintHistory[index]} hint${hintHistory[index] === 1 ? "" : "s"}`
                  return <li key={"SegmentBreakdown_order_list_item" + index}>{value.getVerseIdentifier()} scored {guessScores[index]} {hintText}</li>
               })}
            </ol></div>}
         {!hasSuccessfullyGuessed ? <p><b>This round was skipped</b></p> : <p><b>This round was successfully guessed</b></p>}
      </div>
   )
}

export interface FinishScreenProps {
   verseGameStore: VerseGameStore
}


export const FinishScreen: React.FC<FinishScreenProps> = (props) => {
   const pastGameSegments = useSyncExternalStore(props.verseGameStore.subscribe, props.verseGameStore.getGameSegments)
   // <SegmentBreakdown gameSegment={value} key={"SegmentBreakdown_item_"+index}/>
   return (
      <div className="FinishScreen-container">
         <div className="FinishScreen-header">
            <h2>Finished</h2>
            <p>Here is your total score</p>
         </div>
         <h2 className="FinishScreen-score">{props.verseGameStore.getGameScore()}</h2>
         <p>Here is a breakdown of your gameplay</p>
         <div className="FinishScreen-past-segments-wrapper">
            {pastGameSegments.map((value: VerseGameSegment, index: number) => <SegmentBreakdown gameSegment={value} key={"SegmentBreakdown_item_" + index} roundIndex={index} />)}
         </div>
      </div>
   )
}