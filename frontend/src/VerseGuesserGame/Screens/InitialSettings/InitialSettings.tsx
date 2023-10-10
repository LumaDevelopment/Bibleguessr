import { useMemo, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { BibleVersion } from "../../../DataStructures/Global/BibleVersion"

import "./InitialSettings.css"

export interface InitialSettingsProps {
    activeUserGameSegment: VerseGameSegment
}

export const InitialSettings: React.FC<InitialSettingsProps> = (props) => {

    const allowedSurrondingVerses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getAllowedSurrondingVerses)

    const currentBook = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getCurrentBook)

    return (
        <div className="InitialSettings-container">
            <div className="InitialSettings-header">
                <h2>Game Settings</h2>
                <p>You can update these during the game</p>
            </div>
            <div className="InitialSettings-body">
                <p>Suronding Verses</p>
                <input type="number" min={0} value={allowedSurrondingVerses} onChange={(event) => {
                    props.activeUserGameSegment.setAllowedSurrondingVerses(event.target.value as unknown as number)
                }} />
                <p>Choose A Book</p>
                <select value={currentBook as BibleVersion} onChange={(event) => {
                    props.activeUserGameSegment.setCurrentBook(event.target.value as BibleVersion)
                }}>
                    {(["King James Version", "English Standard Version"] as BibleVersion[]).map((book) => {
                        return <option key={InitialSettings + "_option_" + book}>{book}</option>
                    })}
                </select>
            </div>
        </div>
    )
}

