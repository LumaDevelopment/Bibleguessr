
import { BrowserRouter, Routes, Route } from "react-router-dom";


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<p>Landing and/or/xor/xand Home Page</p>}>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
