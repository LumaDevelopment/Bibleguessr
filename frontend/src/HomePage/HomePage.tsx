import { NavigationBar } from "../NavigationBar/NavigationBar"
import "./HomePage.css"

export const HomePage: React.FC = () => {
    return (
        <div className="HomePage-container">
            <div className="HomePage-card">
                <img src="/image_01.png" className="HomePage-card-image" />
                <div className="HomePage-card-text">
                    <div>
                        <p className="HomePage-title"><b>BibleGuessr</b></p>
                        <p className="HomePage-desc">Study Bible Verses Online</p>
                        <p className="HomePage-play-now-guessed"><span>1,000,500</span> Bible Verses Guessed</p>
                    </div>
                    <a className="HomePage-play-button" href="_blank">Play Now</a>
                </div>
            </div>
            <div className="HomePage-card">
                <div className="HomePage-card-text">
                    <div>
                        <h3>Play in 3D Mode</h3>
                    </div>
                </div>
                <img src="/image_01.png" className="HomePage-card-image" />
            </div>
        </div>
    )
}