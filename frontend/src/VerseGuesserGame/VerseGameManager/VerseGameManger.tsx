import { useMemo, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { VerseGameStore } from "./VerseGameStore"
import { VerseGameScreenSelector } from "../../DataStructures/VerseGuesserGame/VerseGameScreenSelector"
import { InitialSettings } from "../Screens/InitialSettings/InitialSettings"
import "./VerseGameManager.css"
import { VerseGameScreen } from "../Screens/VerseGameScreen/VerseGameScreen"
import { Verse } from "../../DataStructures/Global/Verse"
import { FinishScreen } from "../Screens/FinishScreen/FinishScreen"

export const VerseGameManager: React.FC = () => {
   const gameStore = useMemo(() => {
      let store = new VerseGameStore();
      let firstSegment = new VerseGameSegment(store.getDefaultBibleVersion(), store.getDefaultContextVerses())
      store.addNewGameSegment(firstSegment)
      return store;
   }, [])
   const currentUserScreen: VerseGameScreenSelector = useSyncExternalStore(gameStore.subscribe, gameStore.getCurrentUserScreen)
   console.log("Current Store", gameStore)
   return (
      <>
         {currentUserScreen === "INITIAL SETTINGS" && <InitialSettings verseGameStore={gameStore} />}
         {currentUserScreen === "MAIN GUESSER" && <VerseGameScreen verseGameStore={gameStore} />}
         {currentUserScreen === "FINISH SCREEN" && <FinishScreen verseGameStore={gameStore}/>}
         <div className="VerseGameManager-footer">
            <a className="VerseGameManager-button" onClick={() => {
               gameStore.previousScreen()
            }} href="#">Back</a>
      
            <a className="VerseGameManager-button" onClick={() => {
               gameStore.nextScreen()
            }} href="#">{currentUserScreen === "INITIAL SETTINGS" ? "Next" : "Finish"}</a>
         </div>
      </>
   )
}