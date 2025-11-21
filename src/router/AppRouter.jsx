import React from 'react';
import { Route, Routes } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import RoleRedirect from './RoleRedirect';
import Login from '../pages/Login/Login';
import Unauthorized from '../pages/Unauthorized/Unauthorized';

import AdminManageUsers from '../pages/Admin/ManageUsers';
import ViewSchools from '../pages/Admin/ViewSchools';
import ViewSubjects from '../pages/Admin/ViewSubjects';

import ViewParents from '../pages/Headmaster/ViewParents';
import ViewStudents from '../pages/Headmaster/ViewStudents';
import ViewTeachers from '../pages/Headmaster/ViewTeachers';

import TeacherHome from '../pages/Teacher/TeacherHome';

import Schedule from '../pages/Schedule/Schedule';

import SchoolStatistics from '../pages/Statistics/SchoolStatistics';
import SubjectStatistics from '../pages/Statistics/SubjectStatistics';
import TeacherStatistics from '../pages/Statistics/TeacherStatistics';

import StudentAbsences from '../pages/Absences/StudentAbsences';
import ClassAbsences from '../pages/Absences/ClassAbsences';
import SchoolAbsences from '../pages/Absences/SchoolAbsences';
import AllSchoolsAbsences from '../pages/Absences/AllSchoolsAbsences';

import Complaints from '../pages/Complaints/Complaints';

import StudentGrades from '../pages/Grades/StudentGrades';
import ClassGrades from '../pages/Grades/ClassGrades';
import SchoolGrades from '../pages/Grades/SchoolGrades';
import AllSchoolsGrades from '../pages/Grades/AllSchoolsGrades';



function AppRouter() {
  return (
    <Routes>
      {/* Default redirect to home based on role*/}
      <Route path="/" element={<RoleRedirect/>}/>

      {/* Route to login page */}
      <Route path="/login" element={<Login/>}/>

      {/* Route to unauthorized page */}
      <Route path="/unauthorized" element={<Unauthorized/>} />

      {/* Admin routes */}
      <Route
        path='/admin/manageUsers' 
        element={
          <ProtectedRoute>
            <AdminManageUsers/>
          </ProtectedRoute>
        }
      />

      <Route
        path='/admin/viewSchools' 
        element={
          <ProtectedRoute>
            <ViewSchools/>
          </ProtectedRoute>
        }
      />

      <Route
        path='/admin/viewSubjects' 
        element={
          <ProtectedRoute>
            <ViewSubjects/>
          </ProtectedRoute>
        }
      />

      {/* Headmaster routes */}

      <Route
        path='/headmaster/viewParents' 
        element={
          <ProtectedRoute>
            <ViewParents/>
          </ProtectedRoute>
        }
      />

      <Route
        path='/headmaster/viewStudents' 
        element={
          <ProtectedRoute>
            <ViewStudents/>
          </ProtectedRoute>
        }
      />

      <Route
        path='/headmaster/viewTeachers' 
        element={
          <ProtectedRoute>
            <ViewTeachers/>
          </ProtectedRoute>
        }
      />

      {/* Teacher routes */}
      <Route
        path='/teacher' 
        element={
          <ProtectedRoute>
            <TeacherHome/>
          </ProtectedRoute>
        }
      />

      {/* Schedule route */}
      <Route
        path='/schedule/:classId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER", "TEACHER", "PARENT", "STUDENT"]}>
            <Schedule/>
          </ProtectedRoute>
        }
      />

      {/* Statistics route */}
      <Route
        path='/statistics/:subjectId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER"]}>
            <SubjectStatistics/>
          </ProtectedRoute>
        }
      />

      <Route
        path='/statistics/:teacherId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER"]}>
            <TeacherStatistics/>
          </ProtectedRoute>
        }
      />

      <Route
        path='/statistics/:schoolId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER"]}>
            <SchoolStatistics/>
          </ProtectedRoute>
        }
      />    

      {/* Absences route */}
      <Route
        path='/absences/'
        element={
          <ProtectedRoute roles={["ADMIN"]}>
            <AllSchoolsAbsences/>
          </ProtectedRoute>
        }
      /> 

      <Route
        path='/absences/:schoolId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER"]}>
            <SchoolAbsences/>
          </ProtectedRoute>
        }
      /> 
      <Route
        path='/absences/:classId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER", "TEACHER"]}>
            <ClassAbsences/>
          </ProtectedRoute>
        }
      /> 

      <Route
        path='/absences/:studentId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER", "TEACHER", "PARENT", "STUDENT"]}>
            <StudentAbsences/>
          </ProtectedRoute>
        }
      /> 

      {/* Complaints route */}
      <Route
        path='/complaints/:studentId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER", "TEACHER", "PARENT", "STUDENT"]}>
            <Complaints/>
          </ProtectedRoute>
        }
      />

      {/* Grades routes */}
      <Route
        path='/grades/'
        element={
          <ProtectedRoute roles={["ADMIN"]}>
            <AllSchoolsGrades/>
          </ProtectedRoute>
        }
      /> 

      <Route
        path='/grades/:schoolId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER"]}>
            <SchoolGrades/>
          </ProtectedRoute>
        }
      /> 
      <Route
        path='/grades/:classId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER", "TEACHER"]}>
            <ClassGrades/>
          </ProtectedRoute>
        }
      /> 

      <Route
        path='/grades/:studentId'
        element={
          <ProtectedRoute roles={["ADMIN", "HEADMASTER", "TEACHER", "PARENT", "STUDENT"]}>
            <StudentGrades/>
          </ProtectedRoute>
        }
      /> 

    </Routes>
  )
}

export default AppRouter