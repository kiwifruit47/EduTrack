import React from "react";
import "./Dashboard.css";
import { Outlet } from "react-router-dom";
import Sidebar from "../../../components/Sidebar/Sidebar";

// Sidebar provides navigation to the nested routes
const Dashboard = () => {
    return(
        <div className="main-container">
            <Sidebar/>
            <div className="page-content-container">
                <Outlet/>
            </div>
        </div>
    )
}

export default Dashboard;