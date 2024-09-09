import React from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from '../../api/axios';
import s from "./DailyStudentSchedule.module.css";
import Loader from '../ui/Loader';

const DailyStudentSchedule = () => {
  // Function for fetching the day
  const days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  const currentDay = new Date().getDay();
  const dayName = days[currentDay];

  const fetchSchedule = async () => {
    const res = await axios.get(`/students/schedule/${dayName.toLowerCase()}/first/11a`);
    return res.data;
  };

  const { data: schedule, isLoading, error } = useQuery({
    queryKey: ['schedule', dayName],
    queryFn: fetchSchedule,
    enabled: currentDay > 0 && currentDay < 6,
  });

  if (currentDay === 0 || currentDay === 6) {
    return <div>No schedule available for today.</div>;
  }

  if (isLoading) return <Loader />;

  if (error) return <div>Error loading schedule: {error.message}</div>;

  return (
    <div>
      <h3 className={s.heading}>Today's Schedule</h3>
      <table className={s.scheduleTable}>
        <thead>
          <tr>
            <th className={s.tableHeader}>Period</th>
            <th className={s.tableHeader}>Subject</th>
          </tr>
        </thead>
        <tbody className={s.tableBody}>
          {["FIRST", "SECOND", "THIRD", "FOURTH", "FIFTH", "SIXTH"].map((periodType) => {
            const period = schedule.find(schedule => schedule.periodType === periodType);
            return (
              <tr key={periodType} className={s.tableRow}>
                <td className={s.tableCell}>{periodType}</td>
                <td className={s.tableCell}>{period ? period.subject : 'Free Period'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default DailyStudentSchedule;
