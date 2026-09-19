import { useState } from 'react'
import heroImg from './assets/hero.png'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import './App.css'

import WorldMap from "./components/Map/WorldMap";

function App() {
  return (
    <div>
      <h1>AnyGuessr</h1>
      <WorldMap />
    </div>
  );
}

export default App;