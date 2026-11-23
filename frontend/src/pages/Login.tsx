import "./Login.css";

function Login() {
    return (
        <main className="login">
            <h1>Login</h1>

            <button
                type="button"
                onClick={() => {
                    window.location.href =
                        "http://localhost:8080/oauth2/authorization/cognito";
                }}
            >
                Log in with Cognito
            </button>
        </main>
    );
}

export default Login;
