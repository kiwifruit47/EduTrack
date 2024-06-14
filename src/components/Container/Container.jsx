import React from "react";
import "./Container.css";
import Header from "../Header/Header";

const Container = () => {
    return <div className="dashboard--container">
        <Header/>


        <div className="homeComponentsContainer">
            <div className="homeComponent div1">
                <h3>Schedule</h3>
            </div>
            <div className="homeComponent div2">
                <h3>Absences</h3>
            </div>
            <div className="homeComponent div3">
                <h3>Average grade</h3>
            </div>
            <div className="homeComponent div4">
                <h3>Current shift</h3>
            </div>
        </div>

        
    </div>
}

export default Container;