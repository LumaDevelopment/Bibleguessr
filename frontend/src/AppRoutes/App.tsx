
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { NavigationBar } from "../NavigationBar/NavigationBar";
import { HomePage2 } from "../HomePage2/HomePage2";
import "../NavigationBar/NavigationBar.css"
import "./App.css"
import { VerseGameManager } from "../VerseGuesserGame/VerseGameManager/VerseGameManger";

/**
 * Highest level component of the webpage.
 */
export const App: React.FC = () => {
  return (
    <div className="App-container">
      <NavigationBar />
      {/**
       * Swaps the component based on user URL.
       * As a result, the final build is one HTML file for all routes.
       * If .htaccess does not work for apache, then try using a HashRouter.
       */}
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage2 />}/>
          <Route path="/play" element={<VerseGameManager />}/>
        </Routes>
      </BrowserRouter>
    </div>
  )
}

export default App
