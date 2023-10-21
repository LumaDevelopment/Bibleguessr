import { BibleData } from "../DataStructures/Global/BibleData";
import { Subscribable } from "../DataStructures/Global/Subscribable"
import { VerseGameScreenSelector } from "../DataStructures/VerseGuesserGame/VerseGameScreenSelector";
import { VerseGameSegment } from "../DataStructures/VerseGuesserGame/VerseGameSegment";
import {getServerBibleData} from "../AppRoutes/Middlelayer"

export class VerseGameStore extends Subscribable {
    private bibleData!: BibleData;
    private gameSegments: VerseGameSegment[] = []
    private activeGameSegment!: VerseGameSegment;
    private currentUserScreen: VerseGameScreenSelector = "INITIAL SETTINGS"
    constructor() {
        super();
        // Since async is not allowed in the constructor, the bible data must be loaded in a seperate method.
        this.loadServerBibleData();
    }
    loadServerBibleData = async () => {
        this.bibleData = await getServerBibleData() as BibleData
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
    addGameSegment = (newSegment: VerseGameSegment) => {
        this.gameSegments = [...this.gameSegments, newSegment]
        this.activeGameSegment = newSegment
        this.emitChange()
    }
    getBibleData = (): BibleData => {
      return this.bibleData;
    }

    
    addNewGameSegment = (newSegment: VerseGameSegment) => {
        this.gameSegments = [...this.gameSegments, this.activeGameSegment]
        this.activeGameSegment = newSegment
        this.emitChange()
    }

    nextScreen = () => {
        if (this.currentUserScreen === "INITIAL SETTINGS") {
            this.currentUserScreen = "MAIN GUESSER"
        }
        this.emitChange();
    }

    previousScreen = () => {
        if (this.currentUserScreen === "MAIN GUESSER") {
            this.currentUserScreen = "INITIAL SETTINGS"
        }
        this.emitChange();
    }

}