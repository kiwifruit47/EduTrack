import React, { useState, useEffect, useContext } from 'react';
import "./Header.css";

function useCurrentDate() {
  const [date, setDate] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => {
      setDate(new Date());
    }, 10000);
    return () => {
      clearInterval(timer);
    };
  }, []);

  return date;
}

const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'June', 'July', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

const Header = () => {
  const date = useCurrentDate();

  const time = date.getHours();
  const day = days[date.getDay()];
  const month = months[date.getMonth()];
  const dayOfMonth = date.getDate();

  const greetingMessage = 
      time < 12 ? 'Good Morning'
      : time < 18 ? 'Good Afternoon'
      : 'Good Evening';

  return (
    <header>
      <div className='headerDiv'>
        <h2>{greetingMessage}!</h2>
      </div>
      <div className='headerDiv'>
        <h3>{day}, {dayOfMonth} {month}</h3>
      </div>
    </header>
  );
}

export default Header;
