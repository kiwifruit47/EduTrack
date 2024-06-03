import React from "react";
import { BiHome, BiLogOut } from "react-icons/bi";
import { CgProfile } from "react-icons/cg";
import { FaRegStar } from "react-icons/fa";
import { FaRegCalendar } from "react-icons/fa6";
import { IoSettingsOutline } from "react-icons/io5";

import "./Sidebar.css";

const Sidebar = () => {
    return <div className="menu">
        <div className="logo--sidebar">
            <div className="logo--bcg">
                <a href="#">
                    <img src="../../images/logo-full.jpg" alt="logo"/>
                </a>
            </div>
        </div>
        <div className="menu--list">
            <a href="" className="menu--item">
                <CgProfile />
                Name
            </a>
            <a href="" className="menu--item">
                <BiHome />
                Home
            </a>
            <a href="" className="menu--item">
                <FaRegStar />
                Grades
            </a>
            <a href="" className="menu--item">
                <FaRegCalendar />
                Attendance
            </a>
            <a href="" className="menu--item">
                <IoSettingsOutline />
                Settings
            </a>
            <a href="" className="menu--item">
                <BiLogOut />
                Logout
            </a>
        </div>
    </div>
}

export default Sidebar;