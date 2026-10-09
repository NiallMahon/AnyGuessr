import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Home from './pages/Home'
import Profile from './pages/Profile'
import './App.css'
import WorldMap from "./components/Map/WorldMap";
import MapSelector from "./pages/MapSelector";
import MapMaker from './pages/MapMaker';


function App() {
  return(
 <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/profile" element={<Profile />} />
        <Route path='/mapmaker' element={<MapMaker />} />
        <Route path='/mapselector' element={<MapSelector />} />
        <Route
          path="/map"
          element={
            <div>
              <h1>AnyGuessr</h1>
              <WorldMap />
            </div>
          }
        />
      </Routes>
    </>
  );
}

export default App;
