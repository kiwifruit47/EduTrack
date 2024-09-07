import React, { useState } from "react";
import { createBrowserRouter, createRoutesFromElements, Link, Route, RouterProvider, Outlet, Navigate } from 'react-router-dom';
import RequireAuth from "./components/RequireAuth";
import Login from "./pages/Login/Login";
import Unauthorized from "./components/Unauthorized";
import Dashboard from "./pages/(logged-in)/Dashboard/Dashboard";
import Home from "./components/Home/Home";
import StudentGrades from "./components/Grades/StudentGrades";
import Profile from "./components/Profile/Profile";
import Attendance from "./components/Attendance/Attendance";
import Schedule from "./components/Schedule/Schedule";
import './App.css';

// Adding protected route for Dashboard to prevent access from not logged-in users
// const ProtectedRoute = ({ isAuthenticated }) => {
//   return isAuthenticated ? <Outlet /> : <Navigate to="/login" />;
// }

// Defining root element because of react-router-dom new version requirements
const Root = () => {
  return (
    <>
      <div style={{width: "0px"}}>
        <Link to="/">Home</Link>
      </div>
      <div><Outlet /></div>
    </>
  );
}

// Adding useState Hook for login auth
const App = () => {

  const router = createBrowserRouter(
    createRoutesFromElements(
      <Route path="/" element={<Root />}>
        <Route path="/login" element={<Login />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route element={<RequireAuth allowedRoles={["STUDENT", "PARENT", "TEACHER", "HEADMASTER", "ADMIN"]}/>}>
          <Route path="/" element={<Dashboard />}>
            <Route element={<RequireAuth allowedRoles={["STUDENT", "PARENT", "TEACHER"]}/>}>
              <Route path="home" element={<Home />} />
              <Route path="profile" element={<Profile />} />
              <Route path="schedule" element={<Schedule />} />
              <Route path="grades" element={<StudentGrades />} />
              <Route path="attendance" element={<Attendance />} />
            </Route>
            
          </Route>
        </Route>
      </Route>
    )
  );

  return (
      <div className="App">
        <RouterProvider router={router} />
      </div>
  );
}

export default App;
