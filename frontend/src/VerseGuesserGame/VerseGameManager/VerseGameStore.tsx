import { BibleData } from "../../DataStructures/Global/BibleData";
import { Subscribable } from "../../DataStructures/Global/Subscribable"
import { VerseGameScreenSelector } from "../../DataStructures/VerseGuesserGame/VerseGameScreenSelector";
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment";
import { getServerBibleData } from "../../AppRoutes/Middlelayer"

export class VerseGameStore extends Subscribable {
   private bibleData!: BibleData;
   private gameSegments: VerseGameSegment[] = []
   private activeGameSegment!: VerseGameSegment;
   private currentUserScreen: VerseGameScreenSelector = "INITIAL SETTINGS"
   private defaultContextVerses: number = 5
   private defaultBibleVersion: string = "King James Bible"
   private isProcessingUserGuess: boolean = false
   constructor() {
      super();
      // Since async is not allowed in the constructor, the bible data must be loaded in a separate method.
      this.loadServerBibleData();
   }

   getDefaultContextVerses = () => {
      return this.defaultContextVerses
   }

   getDefaultBibleVersion = () => {
      return this.defaultBibleVersion
   }

   getIsProcessingUserGuess = (): boolean => {
      return this.isProcessingUserGuess;
   }

   getGameSegments = (): VerseGameSegment[] => {
      return this.gameSegments
   }
   getActiveGameSegment = (): VerseGameSegment => {
      return this.activeGameSegment;
   }
   getCurrentUserScreen = (): VerseGameScreenSelector => {
      return this.currentUserScreen
   }
   getBibleData = (): BibleData => {
      return this.bibleData;
   }

   addNewGameSegment = (newSegment: VerseGameSegment) => {
      if (newSegment === undefined) {
         console.log("VerseGameStore | addNewGameSegment | newSegment is undefined, unable to add")
         return;
      }
      if (this.activeGameSegment !== undefined) {
         this.gameSegments = [...this.gameSegments, this.activeGameSegment]
      }
      this.activeGameSegment = newSegment
      this.emitChange()
   }

   loadServerBibleData = async () => {
      this.bibleData = await getServerBibleData() as BibleData
      this.emitChange();
   }



   nextScreen = () => {
      if (this.currentUserScreen === "INITIAL SETTINGS") {
         this.activeGameSegment.initVerses()
         this.currentUserScreen = "MAIN GUESSER"
      }
      else if (this.currentUserScreen === "MAIN GUESSER") {
         this.gameSegments = [...this.gameSegments, this.activeGameSegment]
         this.currentUserScreen = "FINISH SCREEN"
      }
      this.emitChange();
   }

   previousScreen = () => {
      if (this.currentUserScreen === "MAIN GUESSER") {
         this.currentUserScreen = "INITIAL SETTINGS"
      }
      // Note, the user can not go back to their current play session.
      // Its possible and the data is not lost, but it would be best for them to start over.
      else if (this.currentUserScreen === "FINISH SCREEN") {
         // Need to reset this round
         this.gameSegments = []
         this.activeGameSegment = new VerseGameSegment(this.defaultBibleVersion, this.defaultContextVerses)
         this.currentUserScreen = "INITIAL SETTINGS";
      }
      this.emitChange();
   }
   
   setIsProcessingUserGuess = (processing: boolean) => {
      this.isProcessingUserGuess = processing;
   }
}