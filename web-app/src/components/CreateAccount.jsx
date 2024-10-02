import { Box, Button, TextField, Typography, Paper } from "@mui/material"; // Thêm import Paper
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { getToken, setToken, setUserId } from "../services/localStorageService";

export default function CreateAccount() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [repeatPassword, setRepeatPassword] = useState("");
  const [error, setError] = useState("");

  const handleCreateAccount = async (event) => {
    event.preventDefault();
    
    // Xác thực dữ liệu
    if (!validateInputs()) return;

    const data = { email, firstName, lastName, username, password };

    try {
        // Gửi yêu cầu tạo tài khoản
        await fetch("http://localhost:8668/api/v1/identity/users/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        });

        // Thêm delay trước khi thực hiện đăng nhập
        setTimeout(async () => {
            const loginImplicit = {
                username: data.username,
                password: data.password,
            };

            try {
                const loginResponse = await fetch(`http://localhost:8668/api/v1/identity/auth/token`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(loginImplicit),
                });

                if (!loginResponse.ok) {
                    const errorData = await loginResponse.json();
                    setError(errorData.message || "Đăng nhập không thành công.");
                    return;
                }

                const tokenData = await loginResponse.json();
                setToken(tokenData.result?.token);

                // Lấy thông tin người dùng
                const token = getToken();
                const userInfoResponse = await fetch(`http://localhost:8668/api/v1/identity/users/my-info`, {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}`
                    }
                });

                if (!userInfoResponse.ok) {
                    throw new Error("Failed to fetch user info");
                }

                const userData = await userInfoResponse.json();
                setUserId(userData.result.id);

                // Điều hướng đến trang xác thực OTP
                navigate(`/verify-email`);
            } catch (error) {
                setError("Có lỗi xảy ra khi đăng nhập. Vui lòng thử lại.");
                console.error("Error during login:", error);
            }
        }, 2000); // Delay 2 giây trước khi thực hiện đăng nhập

    } catch (error) {
        setError("Có lỗi xảy ra. Vui lòng thử lại.");
        console.error("Error during account creation:", error);
    }
};

  const validateInputs = () => {
    if (!/\S+@\S+\.\S+/.test(email)) {
      setError("Email không hợp lệ.");
      return false;
    }
    if (username.length < 6) {
      setError("Username phải có ít nhất 6 ký tự.");
      return false;
    }
    if (password.length < 8) {
      setError("Password phải có ít nhất 8 ký tự.");
      return false;
    }
    if (password !== repeatPassword) {
      setError("Repeat Password phải giống Password.");
      return false;
    }
    setError(""); // Reset error
    return true;
  };

  return (
    <Box
      display="flex"
      alignItems="center"
      justifyContent="center"
      height="100vh"
      bgcolor={"#f0f2f5"}
    >
      <Paper elevation={3} sx={{ padding: 4, width: '400px' }}> {/* Thêm Paper để tạo outline */}
        <Typography variant="h5" component="h1" gutterBottom>
          Create an Account
        </Typography>
        {error && <Typography color="error">{error}</Typography>} {/* Hiển thị lỗi */}
        <Box component="form" onSubmit={handleCreateAccount} sx={{ mt: 2 }}>
          <TextField
            label="Email"
            variant="outlined"
            fullWidth
            margin="normal"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <Box display="flex" gap={2}>
            <TextField
              label="First Name"
              variant="outlined"
              fullWidth
              margin="normal"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
            />
            <TextField
              label="Last Name"
              variant="outlined"
              fullWidth
              margin="normal"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
            />
          </Box>
          <TextField
            label="Username"
            variant="outlined"
            fullWidth
            margin="normal"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            margin="normal"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <TextField
            label="Repeat Password"
            type="password"
            variant="outlined"
            fullWidth
            margin="normal"
            value={repeatPassword}
            onChange={(e) => setRepeatPassword(e.target.value)}
          />
          <Button type="submit" variant="contained" color="primary" fullWidth>
            Create Account
          </Button>
        </Box>
        <Typography variant="body2" sx={{ mt: 2, textAlign: 'center' }}>
          Nếu đã có tài khoản? {/* Dòng thông báo */}
        </Typography>
        <Button 
          variant="outlined" 
          color="secondary" 
          onClick={() => navigate("/login")} // Điều hướng về trang Login
          fullWidth
        >
          Quay lại trang Login
        </Button>
      </Paper>
    </Box>
  );
}