import { useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import "./VerseGameScreen.css"

export interface VerseGameScreenProps {
    activeUserGameSegment: VerseGameSegment
}

export const VerseGameScreen: React.FC<VerseGameScreenProps> = (props) => {
    const currentBook = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getCurrentBook)
    const allowedSurrondingVerses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getAllowedSurrondingVerses)
    const currentGuessesCount = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getCurrentGuessesCount)
    const previousUserGuesses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getPreviousUserGuesses)
    return (
        <div className="VerseGameScreen-container">
            <h2>Guess the verse</h2>
            <p>{currentBook}</p>
        </div>
    )
}