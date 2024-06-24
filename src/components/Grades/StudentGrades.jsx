import React, { useEffect,  useState, useMemo } from 'react';
import { useTable } from 'react-table';
import axios from 'axios';
import './StudentGrades.css'

const StudentGrades = () => {
    const [data, setData] = useState([]);
    const [averages, setAverages] = useState({ term1: 0, term2: 0, year: 0 });

    useEffect(() => {
        axios.get('http://localhost:4000/grades')
          .then(response => {
            const gradesData = response.data;
            setData(gradesData);
    
            // Calculating average grades
            const term1Grades = gradesData.flatMap(subject => subject.term1.map(grade => grade.value));
            const term2Grades = gradesData.flatMap(subject => subject.term2.map(grade => grade.value));
            const yearGrades = gradesData.map(subject => subject.year);
    
            setAverages({
              term1: term1Grades.reduce((a, b) => a + b, 0) / term1Grades.length,
              term2: term2Grades.reduce((a, b) => a + b, 0) / term2Grades.length,
              year: yearGrades.reduce((a, b) => a + b, 0) / yearGrades.length
            });
          })
          .catch(error => {
            console.error('Error fetching data:', error);
          });
      }, []);

    const columns = useMemo(() => [
        {
          Header: 'Subject',
          accessor: 'subject',
        },
        {
          Header: 'Term I',
          accessor: 'term1',
          Cell: ({ value }) => (
            <div className="gradesCell">
                {value.map((grade, index) => (
                    <div key={index} className={`gradeItem grade-${grade.value}`}>
                    {grade.value}
                    </div>
                ))}
            </div>
          ),
        },
        {
          Header: 'Term II',
          accessor: 'term2',
          Cell: ({ value }) => (
            <div className="gradesCell">
                {value.map((grade, index) => (
                    <div key={index} className={`gradeItem grade-${grade.value}`}>
                    {grade.value}
                    </div>
                ))}
            </div>
          ),
        },
        {
          Header: 'Year',
          accessor: 'year',
          Cell: ({ value }) => (
            <div className={`gradeItem grade-${value}`}>
              {value}
            </div>
          ),
        },
      ], []);

      const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        rows,
        prepareRow,
      } = useTable({ columns, data });

  return (
    <>
      <header className='gradeHeader'>
        <h1>Grades</h1>
      </header>
      <table {...getTableProps()} className='gradeTable'>
        <thead>
          {headerGroups.map(headerGroup => (
            <tr {...headerGroup.getHeaderGroupProps()}>
              {headerGroup.headers.map(column => (
                <th {...column.getHeaderProps()} className='gradeTableHeader'>
                  {column.render('Header')}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody {...getTableBodyProps()}>
          {rows.map(row => {
            prepareRow(row);
            return (
              <tr {...row.getRowProps()}>
                {row.cells.map(cell => (
                  <td {...cell.getCellProps()} className='gradeTableCells'>
                    {cell.render('Cell')}
                  </td>
                ))}
              </tr>
            );
          })}
          <tr>
            <td className='gradeTableCells'>Average</td>
            <td className='gradeTableCells'>
              <div className="gradesCell">
                <div>
                  {averages.term1.toFixed(2)}
                </div>
              </div>
            </td>
            <td className='gradeTableCells'>
              <div className="gradesCell">
                <div>
                  {averages.term2.toFixed(2)}
                </div>
              </div>
            </td>
            <td className='gradeTableCells'>
              <div>
                {averages.year.toFixed(2)}
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </>
  );
};

export default StudentGrades