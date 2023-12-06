import { BibleData } from "../../DataStructures/Global/BibleData";
import { Subscribable } from "../../DataStructures/Global/Subscribable"
import { VerseGameScreenSelector } from "../../DataStructures/VerseGuesserGame/VerseGameScreenSelector";
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment";
import { getServerBibleData } from "../../AppRoutes/Middlelayer"

/**
 * The verse game store is used to manage all of the game segments.
 * This is the management hub for the entire gameplay loop.
 */
export class VerseGameStore extends Subscribable {
   /**
    * The bible data from the backend. Is created from the loadServerBibleData function in this data structure.
    */
   private bibleData!: BibleData;
   /**
    * A list of all past game segments.
    */
   private gameSegments: VerseGameSegment[] = []
   /**
    * The current game segment that the user is currently guessing on.
    */
   private activeGameSegment!: VerseGameSegment;
   /**
    * What screen the VerseGameManager component should be displaying.
    */
   private currentUserScreen: VerseGameScreenSelector = "INITIAL SETTINGS"
   /**
    * The default context verses for the initial settings field.
    */
   private defaultContextVerses: number = 5
   /**
    * The default bible version for the initial settings field.
    */
   private defaultBibleVersion: string = "King James Bible"
   /**
    * When the user makes a guess, it has to retrieve the global index from the backend for that users guessed verse.
    * Whenever this server request is ongoing, this field will be set to true.
    */
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

   /**
    * Sets this new segment as the active game segment, and pushes the previous active game segment to te
    * game segment history.
    * 
    * @param newSegment 
    * @returns 
    */
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

   /**
    * A async method that retrieves the bible data from the server.
    * This will only ever be done once, so the frontend can know when this process is done based on if the attribute is undefined or not.
   */
   loadServerBibleData = async () => {
      this.bibleData = await getServerBibleData() as BibleData
      this.emitChange();
   }

   /**
    * @returns The max game segment score across all PAST game segments
    */
   getGameScore = (): number => {
      let roundScores: number[] = [];
      for (let i = 0; i < this.gameSegments.length; i++) {
         this.gameSegments[i].calculateScore()
         roundScores.push(this.gameSegments[i].getFinalRoundScore())
      }
      return Math.max(...roundScores)
   }

   /**
    * The logic to send the user to the next screen.
    */
   nextScreen = () => {
      // If the user is on settings, start up the main guessing screen and init the random verse.
      if (this.currentUserScreen === "INITIAL SETTINGS") {
         this.activeGameSegment.initVerses()
         this.currentUserScreen = "MAIN GUESSER"
      }
      // If the user is on the main guesser, the user has finished the game.
      else if (this.currentUserScreen === "MAIN GUESSER") {
         if (this.activeGameSegment.getGuesses() !== 0 || this.gameSegments.length === 0) {
            // Save this active game segment only if the user attempted it.
            this.gameSegments = [...this.gameSegments, this.activeGameSegment]
         }
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