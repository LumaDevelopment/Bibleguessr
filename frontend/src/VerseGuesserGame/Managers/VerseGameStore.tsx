import { BibleVersion } from "../../DataStructures/Global/BibleVersion";
import { Subscribable } from "../../DataStructures/Global/Subscribable"
import { VerseGameScreen } from "../../DataStructures/VerseGuesserGame/VerseGameScreen";
import { VerseGameSegment } from "../../DataStructures/VerseGuesserGame/VerseGameSegment";

export class VerseGameStore extends Subscribable {
    private gameSegments: VerseGameSegment[] = []
    private activeGameSegment!: VerseGameSegment;
    private currentUserScreen: VerseGameScreen = "INITIAL SETTINGS"
    getGameSegments = (): VerseGameSegment[] => {
        return this.gameSegments
    }
    getActiveGameSegment = (): VerseGameSegment => {
        return this.activeGameSegment;
    }
    getCurrentUserScreen = (): VerseGameScreen => {
        return this.currentUserScreen
    }


    addGameSegment = (newSegment: VerseGameSegment) => {
        this.gameSegments = [...this.gameSegments, newSegment]
        this.emitChange()
    }
    setActiveGameSegment = (newSegment: VerseGameSegment) => {
        this.activeGameSegment = newSegment
        this.emitChange()
    }
    setCurrentUserScreen = (currentUserScreen: VerseGameScreen) => {
        this.currentUserScreen = currentUserScreen;
        this.emitChange()
    }

}