import { useMemo, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { VerseGameStore } from "../../VerseGameStore"
import { VerseGameScreenSelector } from "../../../DataStructures/VerseGuesserGame/VerseGameScreenSelector"
import { InitialSettings } from "../InitialSettings/InitialSettings"
import "./VerseGameManager.css"
import { VerseGameScreen } from "../VerseGameScreen/VerseGameScreen"
import { Verse } from "../../../DataStructures/Global/Verse"

export const VerseGameManager: React.FC = () => {
    const gameStore = useMemo(() => {
        let store = new VerseGameStore();
        let firstSegment = new VerseGameSegment("King James Version",5)
        store.addGameSegment(firstSegment)
        return store;
    }, [])
    const activeUserGameSegment: VerseGameSegment = useSyncExternalStore(gameStore.subscribe, gameStore.getActiveGameSegment)
    const currentUserScreen: VerseGameScreenSelector = useSyncExternalStore(gameStore.subscribe, gameStore.getCurrentUserScreen)
    return (
        <>
            {currentUserScreen === "INITIAL SETTINGS" && <InitialSettings activeUserGameSegment={activeUserGameSegment} bibleData={gameStore.getBibleData()} />}
            {currentUserScreen === "MAIN GUESSER" && <VerseGameScreen verseGameStore={gameStore} />}
            <div className="VerseGameManager-footer">
                <a className="VerseGameManager-button" onClick={() => {
                    if (currentUserScreen !== "INITIAL SETTINGS") {
                        gameStore.previousScreen()
                    }
                    // Todo: Fix this back logic.
                }} href={currentUserScreen !== "INITIAL SETTINGS" ? "/" : "#"}>Back</a>
                <a className="VerseGameManager-button" onClick={() => gameStore.nextScreen()} href="#">{currentUserScreen === "INITIAL SETTINGS" ? "Next" : "Finish"}</a>
            </div>
        </>
    )
}