import { useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { VerseGameStore } from "./VerseGameStore"

export interface VerseGameSettingsProps {
    activeUserGameSegment: VerseGameSegment
}

export const VerseGameSettings: React.FC<VerseGameSettingsProps> = (props) => {
    const allowedGuesses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getAllowedGuesses)
    const allowedSurrondingVerses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getAllowedSurrondingVerses)

    return (
        <div className="VerseGameSettings-container">
            <input type="number" onChange={(event) => {
                props.activeUserGameSegment.setAllowedGuesses(event.target.value as unknown as number)
            }} />
            <input type="number" onChange={(event) => {
                props.activeUserGameSegment.setAllowedSurrondingVerses(event.target.value as unknown as number)
            }} />
        </div>
    )
}