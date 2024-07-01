import React, { useState } from "react";
import { BiHome, BiLogOut, BiMenu } from "react-icons/bi";
import { CgProfile } from "react-icons/cg";
import { FaRegStar } from "react-icons/fa";
import { FaRegCalendar, FaCheck } from "react-icons/fa6";
import { Link } from 'react-router-dom';
import "./Sidebar.css";

const Sidebar = () => {
    const [isOpen, setIsOpen] = useState(false);

    const handleToggle = () => {
        setIsOpen(!isOpen);
    };

    const handleClose = () => {
        setIsOpen(false);
    };

    const marginStyle = { marginRight: "0.7rem" };
    return (
        <div className="sidebar-container">
            {/* Hamburger Menu */}
            <div className="navbar__toggle" onClick={handleToggle}>
                <BiMenu className="hamburger-icon" />
            </div>

            {/* Sidebar Menu */}
            <div className={`menu ${isOpen ? 'show' : ''}`}>
                <div className="logo--sidebar">
                    <div className="logo--bcg">
                        <Link to="home" onClick={handleClose}><img src="../../images/logo-full.jpg" alt="logo" /></Link>
                    </div>
                </div>
                <div className="menu--list">
                    <div className="menu--item">
                        <Link to="home" onClick={handleClose}><BiHome style={marginStyle} />Home</Link>
                    </div>
                    <div className="menu--item">
                        <Link to="profile" onClick={handleClose}><CgProfile style={marginStyle} />Profile</Link>
                    </div>
                    <div className="menu--item">
                        <Link to="grades" onClick={handleClose}><FaRegStar style={marginStyle} />Grades</Link>
                    </div>
                    <div className="menu--item">
                        <Link to="attendance" onClick={handleClose}><FaCheck style={marginStyle} />Absences</Link>
                    </div>
                    <div className="menu--item">
                        <Link to="schedule" onClick={handleClose}><FaRegCalendar style={marginStyle} />Schedule</Link>
                    </div>
                    <div className="menu--item">
                        <Link to="login" onClick={handleClose}><BiLogOut style={marginStyle} />Logout</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Sidebar;
