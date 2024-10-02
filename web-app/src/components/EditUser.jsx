// web-app/src/components/EditUser.jsx
import React, { useEffect, useState } from "react";
import { Box, Button, TextField, Typography } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { getToken, getUserId, removeUserId } from "../services/localStorageService";
import axios from "axios";

const EditUser = () => {
  const navigate = useNavigate();
  const { userId } = useParams();
  const [userData, setUserData] = useState({
    firstName: '',
    lastName: '',
    dob: '',
    city: '',
  });
  const [isDirty, setIsDirty] = useState(false);

  useEffect(() => {
    const userId = getUserId() // Lấy userId đã được lưu
    const accessToken = getToken(); // Lấy access token
    // Hàm gọi API để lấy dữ liệu user profile\
    console.log("Fetching data for User ID:", userId);
    const fetchUserData = async () => {
        const response = await fetch(`http://localhost:8668/api/v1/profile/users/${userId}`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${accessToken}`,
          }
        });

        if (response.ok) {
          const data = await response.json();
          console.log(data.result)
          setUserData(data.result); // Giả sử response trả về có dạng { result: ... }
        } else {
          navigate("/dashboard/manage-users")
        }
    };

    // Gọi hàm fetchUserData
    fetchUserData();
  }, [userId,navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserData({ ...userData, [name]: value });
    setIsDirty(true); // Đánh dấu là đã thay đổi
  };

  const handleSave = async () => {
    const userId = getUserId();
    const accessToken = getToken();
    if (isDirty) {
      if (window.confirm("Bạn có chắc chắn muốn lưu các thay đổi không?")) {
        try {
          console.log('Data to be sent:', userData); // Kiểm tra dữ liệu trước khi gửi
          const response = await axios.put(`http://localhost:8668/api/v1/profile/users/${userId}`, {
            // Dữ liệu cần cập nhật
            firstName: userData.firstName,
            lastName: userData.lastName,
            dob: userData.dob,
            city: userData.city,
          }, {
            headers: {
              Authorization: `Bearer ${accessToken}`,
            },
          });
          console.log('User updated:', response.data);
          // Xử lý sau khi cập nhật thành công (ví dụ: thông báo cho người dùng)
        } catch (error) {
          console.error('Error updating user:', error);
          // Xử lý lỗi (ví dụ: thông báo lỗi cho người dùng)
        }
      }
    }
  };

  const handleCancel = () => {
    if (isDirty && window.confirm("Bạn có chắc chắn muốn hủy các chỉnh sửa không?")) {
      removeUserId();
      navigate("/dashboard/manage-users");
    } else {
      removeUserId();
      navigate("/dashboard/manage-users");
    }
  };

  if (!userData) return <div>Loading...</div>;

  return (
    <Box sx={{ padding: 10, bgcolor: '#30384f', color: '#fff' }}>
      <Typography variant="h4" sx={{ color: '#fff' }}>Chỉnh sửa người dùng</Typography>
      <TextField
        label="Username"
        name="username"
        value={userData.username || ""}
        onChange={handleChange}
        fullWidth
        margin="normal"
        disabled
        InputProps={{ style: { color: '#fff'}}}
        sx={{ '& .MuiOutlinedInput-root': { '& fieldset': { borderColor: '#fff' }}}}
        InputLabelProps={{ style: { color: '#bbb' }, shrink : true }}
      />
      <TextField
        label="Email"
        name="email"
        value={userData.email || ""}
        onChange={handleChange}
        fullWidth
        margin="normal"
        disabled
        InputProps={{ style: { color: '#fff' } }} // Thay đổi màu chữ trong TextField
        InputLabelProps={{ style: { color: '#bbb' }, shrink : true }} // Thay đổi màu nhãn
      />
      <TextField
        label="Họ"
        name="firstName"
        value={userData.firstName || ""}
        onChange={handleChange}
        fullWidth
        margin="normal"
        InputProps={{ style: { color: '#fff' } }} // Thay đổi màu chữ trong TextField
        InputLabelProps={{ style: { color: '#bbb' }, shrink : true }} // Thay đổi màu nhãn
      />
      <TextField
        label="Tên"
        name="lastName"
        value={userData.lastName || ""}
        onChange={handleChange}
        fullWidth
        margin="normal"
        InputProps={{ style: { color: '#fff' } }} // Thay đổi màu chữ trong TextField
        InputLabelProps={{ style: { color: '#bbb' }, shrink : true }} // Thay đổi màu nhãn
      />
      <TextField
        label="Ngày sinh"
        name="dob"
        type="date"
        value={userData.dob || ""}
        onChange={handleChange}
        fullWidth
        margin="normal"
        InputLabelProps={{ style: { color: '#bbb' } }} // Thay đổi màu nhãn
        InputProps={{ 
          style: { color: '#fff' }, // Thay đổi màu chữ trong TextField
          // Thay đổi màu icon
          inputProps: {
            style: { color: '#fff' }, // Màu chữ trong ô nhập
          },
        }} 
      />
      <TextField
        label="Thành phố"
        name="city"
        value={userData.city || ""}
        onChange={handleChange}
        fullWidth
        margin="normal"
        InputProps={{ style: { color: '#fff' } }} // Thay đổi màu chữ trong TextField
        InputLabelProps={{ style: { color: '#bbb' }, shrink : true }} // Thay đổi màu nhãn
      />
      <Box sx={{ marginTop: 2 }}>
        <Button variant="contained" color="primary" onClick={handleSave}>
          Lưu
        </Button>
        <Button variant="outlined" color="secondary" onClick={handleCancel} sx={{ marginLeft: 2 }}>
          Quay lại
        </Button>
      </Box>
    </Box>
  );  
};

export default EditUser;