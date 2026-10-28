import "./Register.css";
import { useState, type SubmitEvent } from "react";
import { Link, useNavigate } from "react-router-dom";

type RegisterResponse = {
    token: string;
    userId: number;
};

function Register() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [location, setLocation] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (event: SubmitEvent<HTMLFormElement>) => {
        event.preventDefault();
        setError("");

    if (password !== confirmPassword) {
        setError("Passwords do not match");
        return;
    }

    try {
        const response = await fetch(
        "http://localhost:8080/auth/register",
        {
            method: "POST",
            headers: {
            "Content-Type": "application/json",
            },
            body: JSON.stringify({
            username,
            email,
            password,
            location,
            }),
        }
        );

            if (!response.ok) {
            throw new Error("Failed to register");
            }

    const data: RegisterResponse = await response.json();

    sessionStorage.setItem("userId", data.userId.toString());

    navigate("/profile");
  } catch (err) {
    setError("Failed to register. Please try again.");

    if (err instanceof Error) {
      console.error(err.message);
    }
  }
};

    return (
        <main className="register">
            <h1>Register</h1>
            <form className="register-form" onSubmit={handleSubmit}>
                <label htmlFor="username">Username:</label>
                <input
                    type="text"
                    id="username"
                    name="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />

                <label htmlFor="email">Email:</label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label htmlFor="password">Password:</label>
                <input
                    type="password"
                    id="password"
                    name="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <label htmlFor="confirmPassword">Confirm Password:</label>
                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                />
                <label htmlFor="location">Location:</label>
                <input
                type="text"
                id="location"
                name="location"
                value={location}
                onChange={(event) => setLocation(event.target.value)}
                required
                />

                {error && <p className="register-error">{error}</p>}

                <button type="submit">Register</button>
            </form>

            <p>
                Already have an account? 
                <Link to="/login">Login</Link>
            </p>
        </main>
    );
}

export default Register;