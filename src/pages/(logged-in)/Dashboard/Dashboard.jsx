import React from "react";
import "./Dashboard.css";
import Sidebar from "../../../components/Sidebar/Sidebar";
import Container from "../../../components/Container/Container";

const Dashboard = () => {
    return <div className="main--container">
        <Sidebar />
        <Container />
    </div>;
}

export default Dashboard;