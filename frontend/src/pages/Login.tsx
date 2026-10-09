import { useState, type SubmitEvent } from "react";
import { Link } from "react-router-dom";
import "./Login.css";

function Login() {
    const [identifier, setIdentifier] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = async (
        event: SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        setError("");

        console.log({identifier, password});
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
                onChange={(e) => setIdentifier(e.target.value)}
                autoComplete="username"
                required />


                <label htmlFor="password">Password:</label>

                <input type="password"
                id="password"
                name="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
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