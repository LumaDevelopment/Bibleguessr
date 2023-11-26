import { useSyncExternalStore } from "react"
import { VerseGameStore } from "../../VerseGameManager/VerseGameStore"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { Verse } from "../../../DataStructures/Global/Verse"

interface SegmentBreakdown {
   gameSegment: VerseGameSegment
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
   const { gameSegment } = props;
   const verseToGuess: Verse = useSyncExternalStore(gameSegment.subscribe, gameSegment.getVerseToGuess) as Verse;
   const guesses = useSyncExternalStore(gameSegment.subscribe, gameSegment.getGuesses);
   const pastGuesses = useSyncExternalStore(gameSegment.subscribe, gameSegment.getPreviousGuesses)
   return (
      <div className="SegmentBreakdown-container">
         <p>Actual Verse: {verseToGuess.getVerseIdentifier()}</p>
         <p>Round Score: 5000 out of 4000</p>
         {guesses > 1 && <><p>Guesses:</p>
            <ol>
               {pastGuesses.map((value: Verse) => {
                  return <li>{value.getVerseIdentifier()} at distance {value.getDistance(verseToGuess)}</li>
               })}
            </ol></>}
      </div>
   )
}

export interface FinishScreenProps {
   verseGameStore: VerseGameStore
}


export const FinishScreen: React.FC<FinishScreenProps> = (props) => {
   const pastGameSegments = useSyncExternalStore(props.verseGameStore.subscribe, props.verseGameStore.getGameSegments)
   return (
      <div className="FinishScreen-container">
         <div className="FinishScreen-header">
            <h2>Finished</h2>
            <p>Here is your total score</p>
         </div>
         <h2 className="FinishScreen-score">4000 / 5000</h2>
         <p>Here is a breakdown of your gameplay</p>
         {pastGameSegments.map(value => <SegmentBreakdown gameSegment={value}/>)}
      </div>
   )
}