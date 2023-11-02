
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { NavigationBar } from "../NavigationBar/NavigationBar";
import { HomePage2 } from "../HomePage2/HomePage2";
import "../NavigationBar/NavigationBar.css"
import "./App.css"
import { VerseGameManager } from "../VerseGuesserGame/Screens/VerseGameManager/VerseGameManger";

export const App: React.FC = () => {
  return (
    <div className="App-container">
      <NavigationBar />
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
