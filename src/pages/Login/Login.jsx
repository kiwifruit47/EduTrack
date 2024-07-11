import React, { useState, useRef, useEffect } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom"; // Import useNavigate
import axios from "../../api/axios";
import useAuth from "../../hooks/useAuth";
import "./Login.css";

const Login = () => {
    const { setAuth } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const from = location.state?.from?.pathname || "/home";
    const userRef = useRef();
    const errorRef = useRef();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMsg, setErrorMsg] = useState('');


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
            const response = await axios.post("/auth/login", 
                JSON.stringify({ username, password }),
                {
                    headers: { "Content-Type": "application/json" },
                    withCredentials: true
                }
            );
            console.log("Response received:", response);
            console.log("Response data:", JSON.stringify(response?.data));
            const accessToken = response?.data?.token
            axios.defaults.headers.common.Authorization = `Bearer ${accessToken}`
            localStorage.setItem('tokenKey', accessToken)
            const { role, id } = response?.data;
            setAuth({ username, password, role, id, accessToken });
            setUsername("");
            setPassword("");
            navigate(from, { replace: true });
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
    )
};

export default Login;
