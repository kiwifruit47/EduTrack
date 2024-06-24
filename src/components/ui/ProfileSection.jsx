import React from 'react';

const ProfileSection = ({ title, children }) => (
  <div className="profile-section">
    <h2>{title}</h2>
    {children}
  </div>
);

export default ProfileSection;
