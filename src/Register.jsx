import React, { useState } from "react"

export const Register = (props) => {
    const [username, setUsername] = useState('');
    const [pass, setPass] = useState('');
    const [fullName, setFullName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [email, setEmail] = useState('');

    const handleSubmit = (e) => {
        e.preventDefaut();
        console.log(username);
    }
 
    return (
        <div className="auth-form-container">
            <h1>Register</h1>
            <form className="register-form">
                <label htmlFor="username">Username</label>
                <input value={username} onChange={(e) => setPass(e.target.value)} type="text" name="username" placeholder="JohnDoe123" id="username"/>
                <small id="usernameHelpBlock">Username should contain only letters and numbers.</small>
                <label htmlFor="password">Password</label>
                <input value={pass} onChange={(e) => setUsername(e.target.value)} type="password" name="password" placeholder="**********" id="password"/>
                <small id="passwordHelpBlock">Password should contain only letters and numbers.</small>
                <label htmlFor="fullName">Full Name</label>
                <input value={fullName} onChange={(e) => setFullName(e.target.value)} type="text" name="fullName" placeholder="John Doe" id="fullName"/>
                <small id="fullNameHelpBlock">Enter first and last name.</small>
                <label htmlFor="phoneNumber">Phone Number</label>
                <input value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} type="text" name="phoneNumber" placeholder="0888888888" id="phoneNumber"/>
                <label htmlFor="email">Email</label>
                <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" name="email" placeholder="john.doe@example.com" id="email"/>
                <button className="btn-main">Register</button>
            </form>
            <button onClick={() => props.onFormSwitch('login')}>Already have an account? Login</button>
        </div>
    )
}