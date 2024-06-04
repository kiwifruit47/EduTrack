import React, { useState} from "react";
import { Login } from "./pages/Login/Login";
import { Register } from "./pages/Register/Register";
import Dashboard from "./pages/(logged-in)/Dashboard/Dashboard";
import './App.css'


const App = () => {
  const [currentForm, setCurrentForm] = useState('login');
  const toggleForm = (formName) => {
    setCurrentForm(formName);
  }

  return (
    <div className="App">
      {
        // currentForm == 'login' ? <Login onFormSwitch = {toggleForm}/> : <Register onFormSwitch = {toggleForm}/>
        <Dashboard />
      }
    </div>
  )
}

export default App