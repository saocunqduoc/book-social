// web-app/src/components/DashBoard.jsx
import React from "react";
import { Box, Drawer, List, ListItem, ListItemText, Avatar } from "@mui/material";
import { Outlet, useNavigate } from "react-router-dom";
import { logOut } from "../services/authenticationService";

const Dashboard = () => {
  const navigate = useNavigate();

  const handleNavigation = (path) => {
    navigate(path);
  };

  const handleLogout = () => {
    logOut();
    window.location.href = "/login";
  }
  return (
    <Box sx={{ display: "flex", bgcolor: '#40569c' }}>
      <Drawer 
        variant="permanent"
        sx={{ 
          width: 170, 
          flexShrink: 0, 
          bgcolor: '#1e1e1e', // Màu nền tối
          zIndex: 2, 
          marginTop: '64px' 
        }}
      >
        <Box sx={{ bgcolor: '#40569c', display: 'flex', alignItems: 'center', p: 2 }}>
          <Avatar alt="User Avatar" src="/path/to/avatar.jpg" />
        </Box>
        <List sx={{ bgcolor: '#40569c', height: '100%' }}>
          <ListItem button onClick={() => handleNavigation("/dashboard/manage-users")}>
            <ListItemText primary="Quản lý người dùng" />
          </ListItem>
          <ListItem button onClick={() => handleNavigation("/dashboard/manage-posts")}>
            <ListItemText primary="Quản lý Bài viết" />
          </ListItem>
          <ListItem button onClick={() => handleNavigation("/admin")}>
            <ListItemText primary="Admin" />
          </ListItem>
          <ListItem button onClick={handleLogout}>
            <ListItemText primary="Logout" />
          </ListItem>
        </List>
      </Drawer>
      <Box
        component="main"
        sx={{ 
          flexGrow: 2, 
          bgcolor: "#1e1e1e", // Màu nền chính
          p: 3
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};

export default Dashboard;