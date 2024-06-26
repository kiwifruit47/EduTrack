import React from 'react';
import { useQuery } from '@tanstack/react-query';
import axiosInstance from '../../api';
import { IoPersonCircle } from "react-icons/io5";
import { Hourglass } from 'react-loader-spinner';
import "./Profile.css";

const userId = 1;

const Profile = () => {
  const { data: userData, error, isLoading } = useQuery({
    queryKey: ["user", userId],
    queryFn: async () => {
      const res = await axiosInstance.get(`/users/${userId}`);
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

  let userRole = userData?.roleType;
  let userRoleForHeadingElement = userRole?.charAt(0).toUpperCase() + userRole?.slice(1);

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
        <span>{userData?.roleType}</span><br />
      </section>
      <section className='role-information'>
        <h2>{userRoleForHeadingElement} Information</h2>
        
      </section>
    </div>
  );
}

export default Profile;
