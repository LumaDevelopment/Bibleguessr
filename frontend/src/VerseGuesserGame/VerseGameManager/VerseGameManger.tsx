import { useMemo, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { VerseGameStore } from "./VerseGameStore"
import { VerseGameScreenSelector } from "../../DataStructures/VerseGuesserGame/VerseGameScreenSelector"
import { InitialSettings } from "../Screens/InitialSettings/InitialSettings"
import "./VerseGameManager.css"
import "../../../src/DataStructures/Global/Buttons.css"
import { VerseGameScreen } from "../Screens/VerseGameScreen/VerseGameScreen"
import { Verse } from "../../DataStructures/Global/Verse"
import { FinishScreen } from "../Screens/FinishScreen/FinishScreen"
import { useNavigate } from 'react-router-dom';


export const VerseGameManager: React.FC = () => {
   const gameStore = useMemo(() => {
      let store = new VerseGameStore();
      let firstSegment = new VerseGameSegment(store.getDefaultBibleVersion(), store.getDefaultContextVerses())
      store.addNewGameSegment(firstSegment)
      return store;
   }, [])
   const currentUserScreen: VerseGameScreenSelector = useSyncExternalStore(gameStore.subscribe, gameStore.getCurrentUserScreen)
   // const navigationHandler = useNavigate();
   const backButtonColor = "Block-button " + (currentUserScreen === "FINISH SCREEN" ? "Block-button-blue" : currentUserScreen === "INITIAL SETTINGS" ? "Block-button-blue" : "Block-button-red")
   const nextButtonColor = "Block-button " + (currentUserScreen === "INITIAL SETTINGS" ? "Block-button-green" : currentUserScreen === "MAIN GUESSER" ? "Block-button-blue" : "Block-button-green")
   return (
      <>
         {currentUserScreen === "INITIAL SETTINGS" && <InitialSettings verseGameStore={gameStore} />}
         {currentUserScreen === "MAIN GUESSER" && <VerseGameScreen verseGameStore={gameStore} />}
         {currentUserScreen === "FINISH SCREEN" && <FinishScreen verseGameStore={gameStore} />}
         <div className="Block-button-wrapper">
            <button className={backButtonColor} onClick={() => {
               if (currentUserScreen === "INITIAL SETTINGS") {
                  // navigationHandler('/');
               } else {
                  gameStore.previousScreen()
               }
            }}>{currentUserScreen === "INITIAL SETTINGS" ? "Home" : "Exit"}</button>
            {currentUserScreen !== "FINISH SCREEN" && <button className={nextButtonColor} onClick={() => {
                  gameStore.nextScreen()
            }}>{currentUserScreen === "INITIAL SETTINGS" ? "Next" : "Finish"}</button>}
         </div>
      </>
   )
}