import { NavigationBar } from "../NavigationBar/NavigationBar"
import "./HomePage.css"

export const HomePage: React.FC = () => {
    return (
        <div className="HomePage-container">
            <NavigationBar />
            <div className="HomePage-main">
                <div className="HomePage-main-card HomePage-play-now">
                    <div>
                        <img src="/image_01.png" className="HomePage-image" />
                    </div>
                    <div className="HomePage-play-now-right">
                        <div>
                            <p className="HomePage-play-now-title"><b>BibleGussr</b></p>
                            <p className="HomePage-play-now-desc">Study Bible Verses For Free</p>
                            <p className="HomePage-play-now-guessed"><span>1,000,500</span> Bible Verses Guessed</p>
                        </div>
                        <a className="HomePage-play-button" href="_blank">Play Now</a>
                    </div>
                </div>
            </div>
        </div>
    )
}