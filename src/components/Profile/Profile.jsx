import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { IoPersonCircle } from "react-icons/io5";
import "./Profile.css";
import axios from '../../api/axios';
import useAuth from '../../hooks/useAuth';
import Loader from '../ui/Loader';


const Profile = () => {
  const { auth } = useAuth();
  const { role, username } = auth;
  const { data: userData, error, isLoading } = useQuery({
    queryKey: ["user", username],
    queryFn: async () => {
      const res = await axios.get(`/${role.toLowerCase()}s/${username}`);
      return res.data;
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
        <span>{`${userData?.firstName} ${userData?.lastName}`}</span><br />
        <label htmlFor="role">Role: </label>
        <span>{role.toLowerCase()}</span><br />
      </section>
      <section className='role-information'>
        <h2>{userRoleForHeadingElement} Information</h2>
        <label htmlFor="class">Class: </label>
        <span>{userData?.className}</span><br />
        <label htmlFor="class">Parent: </label>
        <span>{`${userData?.parentFirstName} ${userData?.parentLastName}`}</span><br />
      </section>
    </div>
  );
}

export default Profile;
