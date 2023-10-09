import { useMemo, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { VerseGameStore } from "../Managers/VerseGameStore"
import { VerseGameScreen } from "../../DataStructures/VerseGuesserGame/VerseGameScreen"
import { InitialSettings } from "./InitialSettings"
import "./VerseGameManager.css"

export const VerseGameManager: React.FC = () => {
    const gameStore = useMemo(() => {
        let store = new VerseGameStore();
        let firstSegment = new VerseGameSegment(0)
        store.addGameSegment(firstSegment)
        return store;
    }, [])
    const activeUserGameSegment: VerseGameSegment = useSyncExternalStore(gameStore.subscribe, gameStore.getActiveGameSegment)
    const currentUserScreen: VerseGameScreen = useSyncExternalStore(gameStore.subscribe, gameStore.getCurrentUserScreen)
    return (
        <>
            {currentUserScreen === "INITIAL SETTINGS" && <InitialSettings activeUserGameSegment={activeUserGameSegment} />}
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