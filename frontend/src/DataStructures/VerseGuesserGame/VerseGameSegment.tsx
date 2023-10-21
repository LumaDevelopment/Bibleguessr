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
    private bibleVersion!: string
    private guesses: number = 0;
    /**
     * Verses that are above the verse to guess (higher global number)
     */
    private contextVersesAbove: Verse[] = [];
    /**
     * Verses that are below the verse to guess (higher global number)
     */
    private contextVersesBelow: Verse[] = [];
    private verseToGuess!: Verse
    constructor(bibleVersion: string, contextVersesAbove: Verse[], contextVersesBelow: [], verseToGuess: Verse) {
        super()
        this.bibleVersion = bibleVersion;
        this.contextVersesAbove = [...contextVersesAbove];
        this.contextVersesBelow = [...contextVersesBelow];
        this.verseToGuess = verseToGuess;
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
    getContextVersesBelow= (): Verse[] => {
        return this.contextVersesBelow;
    }
    getVerseToGuess = (): Verse => {
        return this.verseToGuess;
    }
}