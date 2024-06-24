import React, { useState} from "react";
import { createBrowserRouter, createRoutesFromElements, Link, Route, RouterProvider, Outlet, Navigate } from 'react-router-dom';
import { Login } from "./pages/Login/Login";
import Dashboard from "./pages/(logged-in)/Dashboard/Dashboard";
import Home from "./components/Home/Home";
import StudentGrades from "./components/Grades/StudentGrades";
import Profile from "./components/Profile/Profile";
import Attendance from "./components/Attendance/Attendance";
import Schedule from "./components/Schedule/Schedule";
import './App.css'

// Addind protected route for Dashboard to prevent access from not logged-in users
const ProtectedRoute = ({ isAuthenticated }) => {
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" />;
}


// Defining root element because of react-router-dom new version requirements
const Root = () => {
  return(
    <>
      <div>
        <Link to="/"></Link>
      </div>
      <div><Outlet/></div>
    </>
  );
}

// Adding useState Hook for login auth
const App = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(true);

  return (
    <div className="App">
       <RouterProvider router={router}/>
    </div>
  )
}

// setting up routing functionality, Dashboard component is displaying all pages for authenticated users
const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/" element={<Root/>}>
      <Route path="/login" element={<Login setIsAuthenticated={setIsAuthenticated} />}/>
      <Route element={<ProtectedRoute isAuthenticated={isAuthenticated} />}>
        <Route path="/" element={<Dashboard/>}>
          <Route path="home" element={<Home/>}/>
          <Route path="profile" element={<Profile/>}/>
          <Route path="schedule" element={<Schedule/>}/>
          <Route path="grades" element={<StudentGrades/>}/>
          <Route path="attendance" element={<Attendance/>}/>
        </Route>
      </Route>
    </Route>
  )
)

export default App