import React, { useState, useRef, useEffect } from "react"

const Login = (props) => {
    const userRef = useRef();
    const errorRef = useRef();

    const [username, setUsername] = useState('');
    const [pass, setPass] = useState('');
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        userRef.current.focus();
    }, []);

    useEffect(() => {
        setErrorMsg('');
    }, [username, pass]);

    // const handleSubmit = (e) => {
    //     e.preventDefaut();
    //     console.log(username);
    // }
    const handleSubmit = () => {
        e.preventDefaut();
        setIsAuthenticated(true);
      }
 
    return (
        <>
            <p ref={errorRef} className={errorMsg ? "error-msg" : "offscreen"} aria-live="assertive">{errorMsg}</p>
            <div role="form" className="auth-form-container">
                <h1>Login</h1>
                <form className="login-form">
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
                    autoComplete="off"/>
                    <label htmlFor="password">Password</label>
                    <input 
                    value={pass} 
                    onChange={(e) => setPass(e.target.value)} 
                    type="password" 
                    name="password" 
                    placeholder="**********" 
                    id="password"
                    required/>
                    <button onClick={handleSubmit} className="btn-main">Login</button>
                </form>
                <span style={{marginTop: "1.5rem"}}>Don't have an accout? Contact school administration.</span>
            </div>
        </>
    )
}

export default Login