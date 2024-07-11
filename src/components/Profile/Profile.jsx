import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { IoPersonCircle } from "react-icons/io5";
import { Hourglass } from 'react-loader-spinner';
import "./Profile.css";
import axios from '../../api/axios';
import useAuth from '../../hooks/useAuth';


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
    return (
      <div style={{ textAlign: "center", paddingTop: "20rem" }}>
        <Hourglass
          visible={true}
          height="80"
          width="80"
          ariaLabel="hourglass-loading"
          wrapperStyle={{}}
          wrapperClass=""
          colors={['#306cce', '#72a1ed']}
        />
      </div>
    );
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
        <label htmlFor="firstName">First Name: </label>
        <span>{userData?.firstName}</span><br />
        <label htmlFor="lastName">Last Name: </label>
        <span>{userData?.lastName}</span><br />
        <label htmlFor="role">Role: </label>
        <span>{role.toLowerCase()}</span><br />
      </section>
      <section className='role-information'>
        <h2>{userRoleForHeadingElement} Information</h2>
        
      </section>
    </div>
  );
}

export default Profile;
