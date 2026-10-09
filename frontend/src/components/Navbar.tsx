
import { Link } from 'react-router-dom';
import './Navbar.css';

function Navbar() {
  return (
    <nav className="navbar">
        <Link className="navbar-logo" to="/map">
            AnyGuessr
        </Link>
      <div className="navbar-links">
        <Link to="/">Home</Link>
        <Link to="/profile">Profile</Link>
        <Link to="/login">Login</Link>
        <Link to="/mapmaker">MapMaker</Link>
        <Link to="/mapselector">Map Selector</Link>
      </div>
   
      <input
        className="navbar-search"
        type="search"
        placeholder="Search"
      />
    </nav>
  );
}

export default Navbar;