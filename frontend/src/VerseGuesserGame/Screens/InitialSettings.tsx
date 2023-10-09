import { useMemo, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { BibleVersion } from "../../DataStructures/Global/BibleVersion"

import "./InitialSettings.css"

export interface InitialSettingsProps {
    activeUserGameSegment: VerseGameSegment
}

export const InitialSettings: React.FC<InitialSettingsProps> = (props) => {

    const allowedSurrondingVerses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getAllowedSurrondingVerses)

    const currentBook = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getCurrentBook)

    const namesToOptions = useMemo(() => {
        let x = new Map<BibleVersion, string>();
        x.set("ESV", "English Standard Version (ESV)")
        x.set("KJV", "King James Version (KJV)")
        return x;
    }, [])

    const optionsToNames = useMemo(() => {
        let x = new Map<string, BibleVersion>();
        x.set("English Standard Version (ESV)", "ESV")
        x.set("King James Version (KJV)", "KJV")
        return x;
    }, [])


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
                <select value={namesToOptions.get(currentBook as BibleVersion) as string} onChange={(event) => {
                    props.activeUserGameSegment.setCurrentBook(optionsToNames.get(event.target.value as string) as BibleVersion)
                }}>
                    {(["ESV", "KJV"] as BibleVersion[]).map((book) => {
                        return <option key={InitialSettings + "_option_" + book}>{namesToOptions.get(book)}</option>
                    })}
                </select>
            </div>
        </div>
    )
}

