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
    console.log(currentUserScreen)
    return (
        <>
            {currentUserScreen === "INITIAL SETTINGS" && <InitialSettings activeUserGameSegment={activeUserGameSegment} />}
            <a className="VerseGameManager-back-button" href="/">Back</a>
            <a className="VerseGameManager-next-button" href="#">{currentUserScreen === "INITIAL SETTINGS" ? "Next" : "Finish"}</a>
        </>
    )
}