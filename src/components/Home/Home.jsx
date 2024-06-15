import React from 'react';
import "./Home.css";
import Header from "../Header/Header";

const Home = () => {
  return (
    <>
      <Header />
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
    </>
  )
}

export default Home;
