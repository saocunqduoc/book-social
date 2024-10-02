// web-app/src/components/UserManagement.jsx
import React, { useEffect, useState } from "react";
import { Box, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TextField } from "@mui/material";
import { getToken, setUserId } from "../services/localStorageService";
import { useNavigate, Outlet } from "react-router-dom";

const ManageUsers = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [userProfiles, setUserProfiles] = useState([]);
  const [combinedData, setCombinedData] = useState([]);

  const [searchTerm, setSearchTerm] = useState("");
  const [filteredData, setFilteredData] = useState([]);
  
  const getUsers = async (accessToken) => {
    const [userResponse, profileResponse] = await Promise.all([
        fetch(`http://localhost:8668/api/v1/identity/users`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            }
        }),
        fetch(`http://localhost:8668/api/v1/profile/users`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            }
        })
    ]);

    if (!userResponse.ok || !profileResponse.ok) {
      throw new Error("Bạn cần phải đăng nhập");
    }

    const userData = await userResponse.json();
    const profileData = await profileResponse.json();

    if (Array.isArray(userData.result)) {
      setUsers(userData.result);
    }

    if (Array.isArray(profileData.result)) {
      setUserProfiles(profileData.result);
    }
  };

  const handleEdit = (userId) => {
    setUserId(userId);
    navigate(`/dashboard/manage-users/edit/${userId}`);
  };

  const handleSearch = () => {
    const lowercasedFilter = searchTerm.toLowerCase();
    const filteredUsers = combinedData.filter(user => {
      return (
        user.username.toLowerCase().includes(lowercasedFilter) ||
        user.email.toLowerCase().includes(lowercasedFilter) ||
        user.firstName.toLowerCase().includes(lowercasedFilter)
      );
    });
    setFilteredData(filteredUsers);
  };

  useEffect(() => {
    const accessToken = getToken();
    if (!accessToken) {
      navigate("/login");
      return;
    }
    getUsers(accessToken);
  }, [navigate]);

  useEffect(() => {
    const combined = users.reduce((acc, user) => {
      const profile = userProfiles.find(profile => profile.username === user.username) || {};
      acc.push({ ...user, ...profile });
      return acc;
    }, []);
    setCombinedData(combined);
    setFilteredData(combined); 
  }, [users, userProfiles]);

  return (
    <Box>
      <Box sx={{ display: 'flex', marginBottom: 5 }}>
        <TextField
          variant="outlined"
          placeholder="Tìm kiếm người dùng..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{ 
            flexGrow: 1, 
            marginRight: 1,
            '& .MuiOutlinedInput-root': {
              '& fieldset': {
                borderColor: '#fff', // Màu viền
              },
              '&:hover fieldset': {
                borderColor: '#fff', // Màu viền khi hover
              },
              '&.Mui-focused fieldset': {
                borderColor: '#fff', // Màu viền khi focus
              },
            },
            '& .MuiInputBase-input': {
              color: '#fff', // Màu chữ nhập vào
            },
            '& .MuiInputLabel-root': {
              color: '#fff', // Màu nhãn
            },
            '& .MuiInputLabel-root.Mui-focused': {
              color: '#fff', // Màu nhãn khi focus
            },
          }} 
        />
        <Button variant="contained" color="primary" onClick={handleSearch}>
          Tìm kiếm
        </Button>
        <Button variant="contained" color="primary" sx={{ marginLeft: 1 }}>
          Tạo mới
        </Button>
      </Box>
      <TableContainer component={Paper} sx={{ bgcolor: '#2a2a2a', border: '1px solid #444', height: '100%',marginBottom: 5 }}>
        <Table>
          <TableHead>
            <TableRow>
            <TableCell sx={{ bgcolor: '#3a3a3a', color: '#fff' }}>STT</TableCell>
            <TableCell sx={{ bgcolor: '#3a3a3a', color: '#fff' }}>Tên đăng nhập</TableCell>
            <TableCell sx={{ bgcolor: '#3a3a3a', color: '#fff' }}>UserId</TableCell>
            <TableCell sx={{ bgcolor: '#3a3a3a', color: '#fff' }}>Họ và Tên</TableCell>
            <TableCell sx={{ bgcolor: '#3a3a3a', color: '#fff' }}>Email</TableCell>
            <TableCell sx={{ bgcolor: '#3a3a3a', color: '#fff' }}>Vai trò</TableCell>
            <TableCell sx={{ bgcolor: '#3a3a3a', color: '#fff' }}>Hành động</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {combinedData.map((user, index) => (
            <TableRow key={user.username}>
              <TableCell sx={{ color: '#fff' }}>{index + 1}</TableCell>
              <TableCell sx={{ color: '#fff' }}>{user.username}</TableCell>
              <TableCell sx={{ color: '#fff' }}>{user.userId}</TableCell>
              <TableCell sx={{ color: '#fff' }}>{user.lastName + " " + user.firstName || "N/A"}</TableCell>
              <TableCell sx={{ color: '#fff' }}>{user.email}</TableCell>
              <TableCell sx={{ maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: '#fff' }}>
                {Array.isArray(user.roles) && user.roles.map(role => role.name).join(', ')}
              </TableCell>
              <TableCell>
                <Button variant="outlined" onClick={() => handleEdit(user.userId)}>Sửa</Button>
                <Button variant="outlined" color="error" sx={{ marginLeft: 1 }}>Xóa</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
        </Table>
      </TableContainer>
      {/* Outlet để render các route con như EditUser */}
      <Outlet />
    </Box>
  );
};

export default ManageUsers;