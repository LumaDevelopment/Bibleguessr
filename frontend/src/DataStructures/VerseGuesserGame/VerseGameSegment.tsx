import { BibleData } from "../Global/BibleData";
import { Subscribable } from "../Global/Subscribable";
import { Verse } from "../Global/Verse";
import { getRandomVerseGameSegment } from "../../../src/AppRoutes/../AppRoutes/../AppRoutes/Middlelayer"
import { VerseGuess } from "../Global/VerseGuess";

/**
 * Since the user will have the ability to change the settings during a play sesson, then 
 * each time the user does a guessing segment, it will save there settings at that  point.
 * Every part of the game loop will create a new segment.
 * 
 * For example, the user can request more guesses for a  game segment, while keeping the other segments intact.
 * 
 * Every GameSegment must also be subscribable. This allows for the active game instance to keep the UI and the data structure in sync.
 */
export class VerseGameSegment extends Subscribable {
   private bibleVersion: string = "King James Bible"
   private guesses: number = 0;
   private contextVerseDefault: number = 5;
   /**
    * Verses that are above the verse to guess (higher global number)
    */
   private contextVersesAbove: Verse[] = [];
   /**
    * Verses that are below the verse to guess (higher global number)
    */
   private contextVersesBelow: Verse[] = [];
   private previousGuesses: VerseGuess[] = []
   private verseToGuess: Verse | undefined;
   private isLoadingVerses: boolean = false;
   private errorLoadingVerses: boolean = false;
   constructor(bibleVersion: string, contextVerseDefault: number) {
      super()
      this.bibleVersion = bibleVersion;
      this.contextVerseDefault = contextVerseDefault;
   }
   initVerses = () => {
       getRandomVerseGameSegment(this)
   }
   setErrorLoadingVerses = (errorLoadingVerses: boolean) => {
      this.errorLoadingVerses = errorLoadingVerses;
      this.emitChange();
   }
   setIsLoadingVerses = (isLoading: boolean) => {
      this.isLoadingVerses = isLoading;
      this.emitChange();
   }
   setBibleVersion = (version: string) => {
      this.bibleVersion = version
      this.emitChange()
   }
   setVerseToGuess = (verseToGuess: Verse) => {
      this.verseToGuess = verseToGuess;
      this.emitChange();
   }
   setContextVerseDefault = (context: number) => {
      this.contextVerseDefault = context;
      this.contextVersesAbove = []
      this.contextVersesBelow = []
      this.verseToGuess = undefined;
      this.emitChange();
   }
   setContextVersesAbove = (above: Verse[]) => {
      this.contextVersesAbove = [...above]
      this.emitChange();
   }
   setContextVersesBelow = (below: Verse[]) => {
      this.contextVersesBelow = [...below]
      this.emitChange();
   }
   addPreviousGuess = (verseUserGuessed: VerseGuess) => {
      this.guesses+=1;
      this.previousGuesses = [...this.previousGuesses, verseUserGuessed]
      this.emitChange()
   }
   getErrorLoadingVerses = (): boolean => {
      return this.errorLoadingVerses;
   }
   getIsLoadingVerses = (): boolean => {
      return this.isLoadingVerses;
   }
   getContextVersesDefault = (): number => {
      return this.contextVerseDefault;
   }
   getBibleVersion = (): string => {
      return this.bibleVersion;
   }
   getGuesses = (): number => {
      return this.guesses;
   }
   getContextVersesAbove = (): Verse[] => {
      return this.contextVersesAbove;
   }
   getContextVersesBelow = (): Verse[] => {
      return this.contextVersesBelow;
   }
   getVerseToGuess = (): Verse | undefined => {
      return this.verseToGuess;
   }
}