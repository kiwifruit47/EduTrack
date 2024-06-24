import React, { useState } from "react"

export const Login = (props) => {
    const [username, setUsername] = useState('');
    const [pass, setPass] = useState('');

    // const handleSubmit = (e) => {
    //     e.preventDefaut();
    //     console.log(username);
    // }
    const handleSubmit = () => {
        // Perform authentication logic here
        setIsAuthenticated(true);
      }
 
    return (
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
                id="username"/>
                <label htmlFor="password">Password</label>
                <input 
                value={pass} 
                onChange={(e) => setPass(e.target.value)} 
                type="password" 
                name="password" 
                placeholder="**********" 
                id="password"/>
                <button onClick={handleSubmit} className="btn-main">Login</button>
            </form>
            <span style={{marginTop: "1.5rem"}}>Don't have an accout? Contact school administration.</span>
        </div>
    )
}
