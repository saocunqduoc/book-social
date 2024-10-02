import { Box, Button, TextField, Typography, Paper } from "@mui/material"; // Thêm import Paper
import { useState } from "react";
import { useNavigate } from "react-router-dom"; // Chỉ cần useNavigate

const VerifyEmail = () => {
    const navigate = useNavigate();
    const [otp, setOtp] = useState(""); // Trạng thái cho OTP
    const [error, setError] = useState(""); // Trạng thái cho lỗi

    // Lấy userId từ localStorage
    const userId = localStorage.getItem("userId");

    const handleVerifyOtp = async (event) => {
        event.preventDefault();

        try {
            // Xây dựng URL với query parameters
            const response = await fetch(`http://localhost:8668/api/v1/identity/auth/verify?userId=${userId}&otp=${otp}`, {
                method: "POST", // Vẫn sử dụng POST nếu API yêu cầu
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                const errorData = await response.json();
                setError(errorData.message || "Xác thực OTP không thành công."); // Hiển thị thông báo lỗi
                return;
            }

            // Nếu xác thực thành công, điều hướng đến trang chính
            navigate("/"); 
        } catch (error) {
            setError("Có lỗi xảy ra. Vui lòng thử lại."); // Thông báo lỗi chung
            console.error("Error during OTP verification:", error); // Ghi log lỗi
        }
    };

    return (
        <Box
            display="flex"
            alignItems="center"
            justifyContent="center"
            height="100vh"
            bgcolor={"#f0f2f5"}
        >
            <Paper elevation={3} sx={{ padding: 4, width: '400px' }}>
                <Typography variant="h5" component="h1" gutterBottom>
                    Verify Your Email
                </Typography>
                <p>Please enter the OTP sent to your email to verify your account.</p>
                {error && <Typography color="error">{error}</Typography>} {/* Hiển thị lỗi */}
                <Box component="form" onSubmit={handleVerifyOtp} sx={{ mt: 2 }}>
                    <TextField
                        label="Enter OTP"
                        variant="outlined"
                        fullWidth
                        margin="normal"
                        value={otp} // Hiển thị OTP từ input
                        onChange={(e) => setOtp(e.target.value)} // Cập nhật giá trị OTP
                    />
                    <Button type="submit" variant="contained" color="primary" fullWidth>
                        Verify OTP
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
};

export default VerifyEmail;