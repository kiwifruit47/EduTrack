import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { IoPersonCircle } from "react-icons/io5";
import "./Profile.css";
import axios from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import Loader from '../ui/Loader';

const Profile = () => {
  const { auth } = useAuth();
  const { role, username, id } = auth;
  const { data: userData, error, isLoading } = useQuery({
    queryKey: ["user", username],
    queryFn: async () => {
      if(role==='STUDENT') {
        const res = await axios.get(`/${role.toLowerCase()}s/${username}`);
        return res.data;
      } else if (role==='TEACHER') {
        const res = await axios.get(`/${role.toLowerCase()}s/view/${id}`);
        return res.data;
      }
    },
  });
  if (isLoading) {
    return <Loader/>;
  }

  if (error) {
    return <div className="error">Error: {error.message}</div>;
  }

  let userRole = role;
  let userRoleForHeadingElement = userRole?.charAt(0).toUpperCase() + userRole?.slice(1).toLowerCase();

  return (
    <div className="profile">
      <div className='pfp'>
        <IoPersonCircle style={{ color: "#013B67", fontSize: "7em" }} />
      </div>
      <section className='user-information'>
        <h2>User Information</h2>
        <label htmlFor="name">Name: </label>
        <span>{`${role.toLowerCase()==='student' ? userData?.firstName : userData?.teacherFirstName} ${role==='STUDENT' ? userData?.lastName : userData?.teacherLastName}`}</span><br />
        <label htmlFor="role">Role: </label>
        <span>{role.toLowerCase()}</span><br />
      </section>
      <section className='role-information'>
        <h2>{userRoleForHeadingElement} Information</h2>
        {role==='STUDENT' && <label htmlFor="class">Class: </label>}
        {role==='STUDENT' && <span>{userData?.className}</span>}
        {role==='TEACHER' && <label htmlFor="class">School: </label>}
        {role==='TEACHER' && <span>{userData?.schoolName}</span>}
        <br />
        {role==='STUDENT' && <label htmlFor="parent">Parent: </label>}
        {role==='STUDENT' && <span>{`${userData?.parentFirstName} ${userData?.parentLastName}`}</span>}
        {role==='TEACHER' && <label htmlFor="class">Subjects: </label>}
        {role==='TEACHER' && userData?.subjects.map((subject, index) => (
          <span key={index}><br/>{subject.subjectType}</span>))}
        <br />
      </section>
    </div>
  );
}

export default Profile;
