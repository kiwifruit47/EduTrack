import React from 'react';
import './StudentGrade.css';
import GradeTable from '../GradeTable/GradeTable';

const StudentGrade = () => {
  return (
    <>
        <header className='gradeHeader'>
            <h3>Grades</h3>
        </header>
        <GradeTable/>
    </>
  )
}

export default StudentGrade