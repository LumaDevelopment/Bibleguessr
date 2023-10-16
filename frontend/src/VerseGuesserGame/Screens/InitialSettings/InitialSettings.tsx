import { useMemo, useSyncExternalStore } from "react"
import { VerseGameSegment } from "../../../DataStructures/VerseGuesserGame/VerseGameSegment"
import { BibleData } from "../../../DataStructures/Global/BibleData"
import "./InitialSettings.css"

export interface InitialSettingsProps {
   activeUserGameSegment: VerseGameSegment
   bibleData: BibleData
}

export const InitialSettings: React.FC<InitialSettingsProps> = (props) => {
   const allowedSurroundingVerses = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getAllowedSurroundingVerses)

   const currentBook = useSyncExternalStore(props.activeUserGameSegment.subscribe, props.activeUserGameSegment.getVersion)

   return (
      <div className="InitialSettings-container">
         <div className="InitialSettings-header">
            <h2>Game Settings</h2>
            <p>You can update these during the game</p>
         </div>
         <div className="InitialSettings-body">
            <p>Surrounding Verses</p>
            <input type="number" min={0} value={allowedSurroundingVerses} onChange={(event) => {
               props.activeUserGameSegment.setAllowedSurroundingVerses(event.target.value as unknown as number)
            }} />
            <p>Choose A Book</p>
            <select value={currentBook as string} onChange={(event) => {
               props.activeUserGameSegment.setVersion(event.target.value)
            }}>
               {props.bibleData.bibleNames.map((book) => {
                  return <option key={InitialSettings + "_option_" + book}>{book}</option>
               })}
            </select>
         </div>
      </div>
   )
}

