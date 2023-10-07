
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { NavigationBar } from "../NavigationBar/NavigationBar";
import { HomePage2 } from "../HomePage2/HomePage2";
import "../NavigationBar/NavigationBar.css"
import "./App.css"

function App() {
  return (
    <div className="App-container">
      <NavigationBar />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage2 />}>
          </Route>
        </Routes>
      </BrowserRouter>
    </div>
  )
}

export default App
