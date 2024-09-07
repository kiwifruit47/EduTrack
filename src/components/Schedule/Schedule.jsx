import React from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import "./Schedule.css";
import Loader from "../ui/Loader"

const periodTypes = ["FIRST", "SECOND", "THIRD", "FOURTH", "FIFTH", "SIXTH"];

const Schedule = () => {
  const { auth } = useAuth();
  const { role, username } = auth;
  const { data: userData } = useQuery({
    queryKey: ["user", username],
    queryFn: async () => {
      const res = await axios.get(`/${role.toLowerCase()}s/${username}`);
      return res.data;
    },
  });
  const fetchSchedule = async (day) => {
    const res = await axios.get(`/students/schedule/${day.toLowerCase()}/first/${userData.className}`);
    return res.data;
  };

  const { data: monday, isLoading: loadingMonday, error: errorMonday } = useQuery({
    queryKey: ['schedule', 'monday'],
    queryFn: () => fetchSchedule('monday'),
  });

  const { data: tuesday, isLoading: loadingTuesday, error: errorTuesday } = useQuery({
    queryKey: ['schedule', 'tuesday'],
    queryFn: () => fetchSchedule('tuesday'),
  });

  const { data: wednesday, isLoading: loadingWednesday, error: errorWednesday } = useQuery({
    queryKey: ['schedule', 'wednesday'],
    queryFn: () => fetchSchedule('wednesday'),
  });

  const { data: thursday, isLoading: loadingThursday, error: errorThursday } = useQuery({
    queryKey: ['schedule', 'thursday'],
    queryFn: () => fetchSchedule('thursday'),
  });

  const { data: friday, isLoading: loadingFriday, error: errorFriday } = useQuery({
    queryKey: ['schedule', 'friday'],
    queryFn: () => fetchSchedule('friday'),
  });

  const isLoading = loadingMonday || loadingTuesday || loadingWednesday || loadingThursday || loadingFriday;
  const error = errorMonday || errorTuesday || errorWednesday || errorThursday || errorFriday;

  if (isLoading) {
    return <Loader/>;
  }

  if (error) {
    return <div className="error">Error: {error.message}</div>;
  }

  const scheduleData = {
    Monday: monday || [],
    Tuesday: tuesday || [],
    Wednesday: wednesday || [],
    Thursday: thursday || [],
    Friday: friday || [],
  };

  return (
    <div>
      <h2 className="scheduleHeader">Weekly Schedule 11A</h2>
      <table className="scheduleTable">
        <thead className="scheduleTableHeader">
          <tr>
            <th>Day</th>
            {periodTypes.map((period) => (
              <th key={period}>{period}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {Object.entries(scheduleData).map(([day, schedule]) => {
            const periods = Array(6).fill(null);

            schedule.forEach((item) => {
              const periodIndex = periodTypes.indexOf(item.periodType);
              if (periodIndex !== -1) {
                periods[periodIndex] = item.subject;
              }
            });

            return (
              <tr key={day}>
                <td className="scheduleCell">{day}</td>
                {periods.map((subject, index) => (
                  <td className="scheduleCell" key={index}>{subject || ''}</td>
                ))}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default Schedule;
