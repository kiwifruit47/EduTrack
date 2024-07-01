import React, { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom"; // Import useNavigate
import DOMPurify from 'dompurify';
// import axios from "../../api/axios";
import axios from "axios";
import useAuth from "../../hooks/useAuth";

const Login = ({ setIsAuthenticated }) => { // Receive setIsAuthenticated as a prop
    const { setAuth } = useAuth();
    const userRef = useRef();
    const errorRef = useRef();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMsg, setErrorMsg] = useState('');
    const [success, setSuccess] = useState(false);

    const navigate = useNavigate(); // Initialize useNavigate

    useEffect(() => {
        userRef.current.focus();
    }, []);

    useEffect(() => {
        setErrorMsg('');
    }, [username, password]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            console.log("Login attempt with username:", username);
            const sanitizedUsername = DOMPurify.sanitize(username);
            const sanitizedPassword = DOMPurify.sanitize(password);
            const response = await axios.post("http://localhost:8080/auth/login", 
                JSON.stringify({ username: sanitizedUsername, password: sanitizedPassword }),
                {
                    headers: { "Content-Type": "application/json" },
                    withCredentials: true
                }
            );
            console.log("Response received:", response);
            console.log("Response data:", JSON.stringify(response?.data));
            const { roleType } = response?.data; // Extract roleType
            setAuth({ username: sanitizedUsername, password, role: roleType });
            setIsAuthenticated(true); // Update authentication state
            setUsername("");
            setPassword("");
            setSuccess(true);
            navigate('/home'); // Redirect to home page
        } catch (error) {
            console.error("Login error:", error);
            if (!error?.response) {
                setErrorMsg("No server response.");
            } else if (error.response?.status === 400) {
                setErrorMsg('Login failed. Please check your username and password.');
            } else if (error.response?.status === 401) {
                setErrorMsg("Unauthorized");
            } else {
                setErrorMsg("Login failed.");
            }
            errorRef.current.focus();
        }
    };

    return (
        <>
            {success ? (
                <section>
                    <h1>You are logged in!</h1>
                    <br />
                    <p>
                        <a href="#">Go to Home</a>
                    </p>
                </section>
            ) : (
                <>
                    <p ref={errorRef} className={errorMsg ? "error-msg" : "offscreen"} aria-live="assertive">{errorMsg}</p>
                    <div role="form" className="auth-form-container">
                        <h1>Login</h1>
                        <form className="login-form" onSubmit={handleSubmit}>
                            <label htmlFor="username">Username</label>
                            <input
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                type="text"
                                name="username"
                                placeholder="JohnDoe123"
                                id="username"
                                ref={userRef}
                                required
                                autoComplete="off"
                            />
                            <label htmlFor="password">Password</label>
                            <input
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                type="password"
                                name="password"
                                placeholder="**********"
                                id="password"
                                required
                            />
                            <button type="submit" className="btn-main">Login</button>
                        </form>
                        <span style={{ marginTop: "1.5rem" }}>Don't have an account? Contact school administration.</span>
                    </div>
                </>
            )}
        </>
    );
};

export default Login;
