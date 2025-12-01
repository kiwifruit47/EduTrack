import { Navigate } from "react-router-dom";
import useAuth from "../hooks/useAuth";

function RoleRedirect() {
  const { user } = useAuth();

  if (user === null) return <Navigate to="/login" />;

  const redirects = {
    ADMIN: "/admin",
    HEADMASTER: "/headmaster",
    TEACHER: "/teacher",
    STUDENT: "/student",
    PARENT: "/parent",
  };

  return <Navigate to={redirects[user.role]} />;
}

export default RoleRedirect