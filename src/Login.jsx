import React, { useState } from "react"

export const Login = (props) => {
    const [username, setUsername] = useState('');
    const [pass, setPass] = useState('');

    const handleSubmit = (e) => {
        e.preventDefaut();
        console.log(username);
    }
 
    return (
        <div className="auth-form-container">
            <h1>Login</h1>
            <form className="login-form">
                <label htmlFor="username">Username</label>
                <input value={username} onChange={(e) => setPass(e.target.value)} type="text" name="username" placeholder="JohnDoe123" id="username"/>
                <label htmlFor="password">Password</label>
                <input value={pass} onChange={(e) => setUsername(e.target.value)} type="password" name="password" placeholder="**********" id="password"/>
                <button className="btn-main">Login</button>
            </form>
            <button onClick={() => props.onFormSwitch('register')}>Don't have an account? Register</button>
        </div>
    )
}
