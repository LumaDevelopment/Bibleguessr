import { Subscribable } from "../Global/Subscribable";
import { Verse } from "../Global/Verse";

/**
 * Since the user will have the ability to change the settings during a play sesson, then 
 * each time the user does a guessing segment, it will save there settings at that current point.
 * Every part of the game loop will create a new segment.
 * 
 * For example, the user can request more guesses for a current game segment, while keeping the other segments intact.
 * 
 * Every GameSegment must also be subscribable. This allows for the active game instance to keep the UI and the data structure in sync.
 */
export class VerseGameSegment extends Subscribable {
    /**
     * How many surronding verses the user gets.
     */
    private allowedSurrondingVerses!: number
    /**
     * How many guesses the user has currently made.
     */
    private currentGuessesCount: number = 0;

    /**
     * Stores the user's previous guesses.
     */
    private previousUserGuesses: Verse[] = [];

    /**
     * Stores the user's current score.
     */
    private usersCurrentScore: number = 4000;


    constructor(allowedSurrondingVerses: number) {
        super();
        this.allowedSurrondingVerses = allowedSurrondingVerses;
    }

    // Setters

    /**
     * @todo Put Dan's formula here
     * 
     * @returns The User's Store
     */
    calculateScore = () => {
        this.usersCurrentScore = 0;
        this.emitChange();
    }

    increaseGuessCount = () => {
        this.currentGuessesCount += 1;
        this.emitChange()
    }

    addGuessedVerse = (verse: Verse) => {
        this.previousUserGuesses = [...this.previousUserGuesses, verse];
        this.emitChange();
    }

    setAllowedSurrondingVerses = (amount: number) => {
        this.allowedSurrondingVerses = amount;
        this.emitChange();
    }

    // Getters

    getAllowedSurrondingVerses = (): number => {
        return this.allowedSurrondingVerses;
    }

    getPreviousUserGuesses = (): Verse[] => {
        return this.previousUserGuesses;
    }

    getUserScore = (): number => {
        return this.usersCurrentScore
    }

    getCurrentGuessesCount = (): number => {
        return this.currentGuessesCount;
    }

}