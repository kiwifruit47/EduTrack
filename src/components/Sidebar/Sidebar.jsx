import React from "react";
import { BiHome, BiLogOut } from "react-icons/bi";
import { CgProfile } from "react-icons/cg";
import { FaRegStar } from "react-icons/fa";
import { FaRegCalendar, FaCheck } from "react-icons/fa6";
import { Link } from 'react-router-dom';

import "./Sidebar.css";

// Sidebar is displayed for authenticated users and provides navigation logic
const Sidebar = () => {
    const marginStyle = {marginRight: "0.7rem"};
    return <div className="menu">
        <div className="logo--sidebar">
            <div className="logo--bcg">
                <Link to="home"><img src="../../images/logo-full.jpg" alt="logo"/></Link>
            </div>
        </div>
        <div className="menu--list">
            <div className="menu--item">
                <Link to="home"><BiHome style={marginStyle}/>Home</Link>
            </div>
            <div className="menu--item">
                <Link to="profile"> <CgProfile style={marginStyle}/>Profile</Link>
            </div>
            <div className="menu--item">
                <Link to="grades"><FaRegStar style={marginStyle}/>Grades</Link>
            </div>
            <div className="menu--item">
                <Link to="attendance"><FaCheck style={marginStyle}/>Attendance</Link>
            </div>
            <div className="menu--item">
                <Link to="schedule"><FaRegCalendar style={marginStyle}/>Schedule</Link>
            </div>
            <div className="menu--item">
                <Link to="login"><BiLogOut style={marginStyle}/>Logout</Link>
            </div>
        </div>
    </div>
}

export default Sidebar;