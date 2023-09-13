import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './AppRoutes/App.tsx'

// Entry point of the app for vite bundling

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
