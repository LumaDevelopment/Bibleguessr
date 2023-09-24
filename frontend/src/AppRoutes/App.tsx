
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { NavigationBar } from "../NavigationBar/NavigationBar";
import { HomePage } from "../HomePage/HomePage";
import "./App.css"

function App() {
  return (
    <div className="App-container">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />}>
          </Route>
        </Routes>
      </BrowserRouter>
    </div>
  )
}

export default App
