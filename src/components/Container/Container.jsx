import React from "react";
import "./Container.css";
import Sidebar from "../Sidebar/Sidebar";
import Dashboard from "../Dashboard/Dashboard";

const Container = () => {
    return <div className="main--container">
        <Sidebar />
        <Dashboard />
    </div>;
}

export default Container;