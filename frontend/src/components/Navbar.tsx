
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