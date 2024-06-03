import React, { useState} from "react";
import { Login } from "../Login/Login";
import { Register } from "../Register/Register";
import Container from "../../components/Container/Container";
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
        <Container />
      }
    </div>
  )
}

export default App