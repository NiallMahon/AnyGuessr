import { Link } from "react-router-dom";
import './Home.css';

function Home() {
  return (
    <div>
      <h1>Welcome to AnyGuessr!</h1>
      <p>Test your knowledge and guess the answers!</p>
      <br></br>
      <br></br>
      <h1><Link className="map-link" to="/map">MAP</Link></h1>
    </div>
  );
}

export default Home;
