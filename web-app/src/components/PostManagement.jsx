// web-app/src/components/ManagePosts.jsx
import React, { useEffect, useState } from "react";
import { TextField, Box, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from "@mui/material";
import { getToken } from "../services/localStorageService";
import { useNavigate } from "react-router-dom";

const ManagePosts = () => {
  const navigate = useNavigate();
  const [posts, setPosts] = useState([]);
  const [searchTerm, setSearchTerm] = useState(""); // State cho ô tìm kiếm
  const [filteredPosts, setFilteredPosts] = useState([]); // State cho dữ liệu đã lọc
  const getPosts = async (accessToken) => {
    const postResponse = await fetch(`http://localhost:8668/api/v1/post/posts`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
      }
    });
    if (!postResponse.ok) {
      throw new Error("Có lỗi xảy ra khi lấy dữ liệu!");
    }

    const postData = await postResponse.json();
    setPosts(postData.result);
    setFilteredPosts(postData.result); 
  };

  const handleSearch = () => {
    const lowercasedFilter = searchTerm.toLowerCase();
    const filtered = posts.filter(post => {
      return (
        post.content.toLowerCase().includes(lowercasedFilter) || // Tìm kiếm theo nội dung
        post.userId.toString().includes(lowercasedFilter) // Tìm kiếm theo UserId
      );
    });
    setFilteredPosts(filtered);
  };

  useEffect(() => {
    const accessToken = getToken();
    if (!accessToken) {
      navigate("/login");
      return;
    }
    getPosts(accessToken);
  }, [navigate]);

  
  return (
    <Box>
      <Box sx={{ display: 'flex', marginBottom: 2 }}>
        <TextField
          variant="outlined"
          placeholder="Tìm kiếm bài viết..."
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
      </Box>
      <TableContainer component={Paper} sx={{ bgcolor: '#2a2a2a', border: '1px solid #444' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ bgcolor: '#616161', borderRight: '1px solid #444', color: '#fff' }}>STT</TableCell> {/* Cột số thứ tự */}
              <TableCell sx={{ bgcolor: '#616161', borderRight: '1px solid #444', color: '#fff' }}>UserId</TableCell>
              <TableCell sx={{ bgcolor: '#616161', borderRight: '1px solid #444', color: '#fff' }}>Nội dung</TableCell>
              <TableCell sx={{ bgcolor: '#616161', borderRight: '1px solid #444', color: '#fff' }}>Ngày tạo</TableCell>
              <TableCell sx={{ bgcolor: '#616161', borderRight: '1px solid #444', color: '#fff' }}>Ngày sửa</TableCell>
              <TableCell sx={{ bgcolor: '#616161', borderRight: '1px solid #444', color: '#fff' }}>Hành động</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {posts.map((post, index) => ( // Thêm index để hiển thị số thứ tự
              <TableRow key={post.id}>
                <TableCell sx={{ color: '#fff', borderRight: '2px solid #444' }}>{index + 1}</TableCell> {/* Hiển thị số thứ tự */}
                <TableCell sx={{ maxWidth: 210, color: '#fff', borderRight: '1px solid #444' }}>{post.userId}</TableCell>
                <TableCell sx={{ maxWidth: 300, borderRight: '1px solid #444', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: '#fff' }}>
                  {post.content}
                </TableCell>
                <TableCell sx={{ color: '#fff', borderRight: '1px solid #444' }}>{new Date(post.createdDate).toLocaleString()}</TableCell>
                <TableCell sx={{ color: '#fff', borderRight: '1px solid #444' }}>{new Date(post.modifiedDate).toLocaleString()}</TableCell>
                <TableCell>
                  <Button variant="outlined" sx={{ marginRight : '20px', marginLeft  : '20px'}}>Sửa</Button>
                  <Button variant="outlined" color="error" sx={{ marginLeft: 1 }}>Xóa</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default ManagePosts;