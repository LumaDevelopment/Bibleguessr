import { NavigationBar } from "../NavigationBar/NavigationBar"
import "./HomePage2.css"

export const HomePage2: React.FC = () => {
    return (
        <div className="HomePage2-container">
            <div className="HomePage2-banner-container">
                <div className="HomePage2-banner-overlay">
                    <h1 className="HomePage2-banner-title">BibleGuessr</h1>
                    <p className="HomePage2-guessed"><span>1,000,500</span> Bible Verses Guessed</p>
                    <a target="_blank" href="#" className="HomePage2-play-button">Play Now!</a>
                </div>
            </div>
        </div>
    )
}