import { useEffect, useState } from "react"
import { NavigationBar } from "../NavigationBar/NavigationBar"
import "./HomePage2.css"
import { getCount } from "../AppRoutes/Middlelayer"

export const HomePage2: React.FC = () => {
   const [count, setCount] = useState<number>(0);
   useEffect(() => {
      getCount().then((value) => {
         if (value !== undefined) {
            setCount(value)
         }
      }).catch((e) => {
         console.error("Unable to get verse count",e)
      })
   }, [])
    return (
        <div className="HomePage2-container">
            <div className="HomePage2-banner-container">
                <div className="HomePage2-banner-overlay">
                    <h1 className="HomePage2-banner-title">BibleGuessr</h1>
                    <p className="HomePage2-guessed"><span>{count}</span> Bible Verses Guessed</p>
                    <a href="/play" className="HomePage2-play-button">Play Now!</a>
                </div>
            </div>
        </div>
    )
}