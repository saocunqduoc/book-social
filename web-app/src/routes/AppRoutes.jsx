// web-app/src/routes/AppRoutes.jsx
import { Route, Routes, useLocation } from "react-router-dom";
import Login from "../components/Login";
import CreateAccount from "../components/CreateAccount";
import Home from "../components/Home";
import Authenticate from "../components/Authenticate";
import VerifyEmail from "../components/VerifyEmail";
import Dashboard from "../components/DashBoard";
import ManagePosts from "../components/PostManagement";
import ManageUsers from "../components/UserManagement";
import EditUser from "../components/EditUser";
import TopHeader from "../components/header/TopHeader";
import Profile from "../components/Profile";
import MyProfile from "../components/MyProfile";
const AppRoutes = () => {
  const location = useLocation();

  const shouldShowHeader = !(
    location.pathname === '/login' || 
    location.pathname ==='/dashboard/manage-users' || 
    location.pathname.startsWith('/dashboard') 
    || location.pathname.startsWith('/create-account')
  );

  return (
    <div>
      {shouldShowHeader && <TopHeader />}
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/authenticate" element={<Authenticate />} />
        <Route path="/" element={<Home />} />
        <Route path="/dashboard" element={<Dashboard />}>
          <Route path="manage-users" element={<ManageUsers />} />
          <Route path="manage-users/edit/:userId" element={<EditUser />} />
          <Route path="manage-posts" element={<ManagePosts />} />
        </Route>
        <Route path="/create-account" element={<CreateAccount />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        <Route path="/profile/:userId" element={<Profile />} />
        <Route path="/my-profile" element={<MyProfile />} />
      </Routes>
    </div>
  );
};

export default AppRoutes;