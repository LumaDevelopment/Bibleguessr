import { useSyncExternalStore } from "react"
import "./InitialSettings.css"
import { VerseGameStore } from "../../VerseGameManager/VerseGameStore"

export interface InitialSettingsProps {
   verseGameStore: VerseGameStore
}

export const InitialSettings: React.FC<InitialSettingsProps> = (props) => {

   const {verseGameStore} = props

   const bibleData = useSyncExternalStore(verseGameStore.subscribe, verseGameStore.getBibleData)
   const activeUserGameSegment = useSyncExternalStore(verseGameStore.subscribe,verseGameStore.getActiveGameSegment)
   const currentBook = useSyncExternalStore(activeUserGameSegment.subscribe, activeUserGameSegment.getBibleVersion)
   const allowedSurroundingVerses = useSyncExternalStore(activeUserGameSegment.subscribe, activeUserGameSegment.getContextVersesDefault)
   
   return (
      <div className="InitialSettings-container">
         <div className="InitialSettings-header">
            <h2>Game Settings</h2>
            <p>You can update these during the game</p>
         </div>
         {(!bibleData || !activeUserGameSegment) && <div className="InitialSettings-loading"><p><b>Loading Bible Data...</b></p></div>}
         {bibleData && <div className="InitialSettings-body">
            <p>Surrounding Verses</p>
            <input className="InitialSettings-context" type="number" min={0} value={allowedSurroundingVerses} onChange={(event) => {
               activeUserGameSegment.setContextVerseDefault(Number(event.target.value))
            }} />
            <p>Choose A Bible Version</p>
            <select className="InitialSettings-version" value={currentBook as string} onChange={(event) => {
               activeUserGameSegment.setBibleVersion(event.target.value)
            }}>
               {bibleData.bibleNames.map((book) => {
                  return <option key={"InitialSettings_bible_version_option_" + book}>{book}</option>
               })}
            </select>
         </div>}
      </div>
   )
}

