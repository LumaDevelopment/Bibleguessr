import { Subscribable } from "../Global/Subscribable";
import { Verse } from "../Global/Verse";

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
    /**
     * The users selected book.
     */
    private version!: string;
    /**
     * How many Surrounding verses the user gets.
     */
    private allowedSurroundingVerses!: number
    /**
     * How many guesses the user has ly made.
     */
    private GuessesCount: number = 0;

    /**
     * The list of all verses that will be on the screen.
     */
    private verseBody: Verse[] = []

    /**
     * Verse to guess
     */
    private verseToGuess!: Verse

    /**
     * Stores the user's previous guesses.
     */
    private previousUserGuesses: Verse[] = [];

    private correctGuesses: boolean[] = []

    /**
     * Stores the user's  score.
     */
    private usersScore: number = 4000;


    constructor(allowedSurroundingVerses: number, verseBody: Verse[], verseToGuess: Verse) {
        super();
        this.allowedSurroundingVerses = allowedSurroundingVerses;
        this.verseBody = verseBody;
        this.verseToGuess = verseToGuess;
    }

    // Setters

    /**
     * @todo Put Dan's formula here
     * 
     * @returns The User's Store
     */
    calculateScore = () => {
        this.usersScore = 0;
        this.emitChange();
    }

    setVersion = (version: string) => {
        this.version = version;
        this.emitChange();
    }

    increaseGuessCount = () => {
        this.GuessesCount += 1;
        this.emitChange()
    }

    addGuessedVerse = (verse: Verse) => {
        this.previousUserGuesses = [...this.previousUserGuesses, verse];
        this.emitChange();
    }

    setAllowedSurroundingVerses = (amount: number) => {
        this.allowedSurroundingVerses = amount;
        this.emitChange();
    }

    // Getters

    getVerseBody = (): Verse[] => {
      return this.verseBody;
    }
    
    getVerseToGuess = (): Verse => {
      return this.verseToGuess
    }

    getAllowedSurroundingVerses = (): number => {
        return this.allowedSurroundingVerses;
    }

    getPreviousUserGuesses = (): Verse[] => {
        return this.previousUserGuesses;
    }

    getUserScore = (): number => {
        return this.usersScore
    }

    getGuessesCount = (): number => {
        return this.GuessesCount;
    }

    getVersion = ():string => {
        return this.version;
    }

}