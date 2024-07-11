import React from 'react'
import { useLocation, Navigate, Outlet } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

const RequireAuth = ({ allowedRoles }) => {
    const { auth } = useAuth();
    const location = useLocation();
    const roles = Array.isArray(auth?.role) ? auth.role : [auth?.role];

    return (
        roles?.some(role => allowedRoles?.includes(role))
        ? <Outlet/>
        : auth?.username
            ? <Navigate to="/unauthorized" state={{from: location}} replace />
            : <Navigate to="/login" state={{from: location}} replace />
    )
}

export default RequireAuth;
