import React from "react";
import "./Container.css";
import StudentGrade from "../StudentGrade/StudentGrade";
import Home from "../Home/Home";
import Profile from "../Profile/Profile";

const Container = () => {
    return <div className="dashboard--container">
        {/* <Home/> */}
        {/* <Profile/> */}
        <StudentGrade/>
    </div>
}

export default Container;