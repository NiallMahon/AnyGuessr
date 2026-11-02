import { useState, type SubmitEvent } from "react";
import { Link, useNavigate} from "react-router-dom";
import "./Login.css";

function Login() {
    const [identifier, setIdentifier] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();
    const [error, setError] = useState("");

    const handleSubmit = async (
        event: SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        setError("");
        
         try {
        const response = await fetch(
        "http://localhost:8080/login",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          identifier,
          password,
        }),
      }
    );

    if (!response.ok) {
      throw new Error("Invalid email or password");
    }

    const data: { userId: number } =
      await response.json();

    sessionStorage.setItem(
      "userId",
      String(data.userId.toString())
    );
    navigate("/profile");
  } catch (error) {
    setError(
      error instanceof Error
        ? error.message
        : "Login failed"
    );
        }
    };


    return (
        <main className="login">
            <h1>Login</h1>
            <form className="login-form" onSubmit={handleSubmit}>
                <label htmlFor="identifier">Email or Username:</label>
                <input 
                type="text" 
                id="identifier" 
                name="identifier"
                value={identifier}
                onChange={(event) => setIdentifier(event.target.value)}
                autoComplete="username"
                required />


                <label htmlFor="password">Password:</label>

                <input type="password"
                id="password"
                name="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required />

                {error && <p className="login-error">{error}</p>}

                <button type="submit">Login</button>

            </form>

            <p>
                Don't have an account? 
                <Link to="/register">Register here!</Link>
            </p>
        
        </main>
    );
}

export default Login;