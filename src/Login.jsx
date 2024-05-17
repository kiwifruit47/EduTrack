import React, { useState } from "react"

export const Login = () => {
    return (
        <form>
            <label for="username">username</label>
            <input type="text" name="username" placeholder="username" id="username"/>
            <label for="password">password</label>
            <input type="password" name="password" placeholder="**********" id="password"/>
            <button>Login</button>
        </form>
    )
}
