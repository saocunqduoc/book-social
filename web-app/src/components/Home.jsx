import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getToken, setLoginUserId, setUserId } from "../services/localStorageService";
import { Box, Card, CircularProgress, Typography } from "@mui/material";

export default function Home() {
  const navigate = useNavigate();
  const [userDetails, setUserDetails] = useState({});
  const [userProfiles, setUserProfiles] = useState({});

      const getUserDetails = async (accessToken) => {
          const [userResponse, profileResponse] = await Promise.all([
              fetch(`http://localhost:8668/api/v1/identity/users/my-info`, {
                  method: "GET",
                  headers: {
                      "Content-Type": "application/json",
                      "Authorization": `Bearer ${accessToken}`
                  }
              }),
              fetch(`http://localhost:8668/api/v1/profile/users/my-profile`, {
                  method: "GET",
                  headers: {
                      "Authorization": `Bearer ${accessToken}`
                  }
              })
          ]);
          if (!userResponse.ok || !profileResponse.ok) {
              throw new Error("Bạn cần phải đăng nhập");
          }

          const userData = await userResponse.json();
          const profileData = await profileResponse.json();
          setLoginUserId(userData.result.id);
          console.log("user data : ", userData.result);
          console.log("profile data : ", profileData.result);
          setUserDetails(userData.result);
          setUserProfiles(profileData.result);

          if(!userData.result.emailVerified) {
            fetch(`http://localhost:8668/api/v1/identity/users/sendEmail`, {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
              }
            });
            navigate("/verify-email");
          }
      };


    useEffect(() => {
      const accessToken = getToken();
  
      if (accessToken == null || accessToken === undefined || typeof accessToken === 'undefined') {
        navigate("/login");
      }
      if (accessToken) {
        getUserDetails(accessToken);
      }
    }, [navigate]);
  
    const goToProfile = (userId) => {
      setUserId(userId);
      navigate(`/profile/${userId}`);
    };
    useEffect(() => {
      if (userDetails.roles) {
        const isAdmin = userDetails.roles.some(role => role.name === "ADMIN");
        if (isAdmin) {
          navigate("/dashboard/manage-users"); // Chuyển đến trang Dashboard nếu là Admin
        } else {
          navigate("/"); // Chuyển đến Home nếu là User
        }
      }
    }, [userDetails, navigate]);
  return (
    <>
        <div>
            <button 
                id="96d7aae0-7c28-466e-be6b-36ee16d82749" 
                onClick={() => goToProfile("96d7aae0-7c28-466e-be6b-36ee16d82749")}
            >
              saocunqduoc
            </button><button 
                id="ca93bb0d-28d7-4d21-8800-d1900a0a5511" 
                onClick={() => goToProfile("ca93bb0d-28d7-4d21-8800-d1900a0a5511")}
            >
              nguyenvanlinh
            </button>
        </div>
      {userDetails.username && userProfiles.firstName ? (
        <Box
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          height="100vh"
          bgcolor={"#f0f2f5"}
        >
          <Card
            sx={{
              minWidth: 400,
              maxWidth: 500,
              boxShadow: 4,
              borderRadius: 4,
              padding: 4,
            }}
          >
            <Box
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                width: "100%", // Ensure content takes full width
              }}
            >
              <img
                src={userDetails.picture != null ? userDetails.picture : "https://static-00.iconduck.com/assets.00/avatar-default-icon-2048x2048-h6w375ur.png"}
                alt={`${userDetails.username}'s profile`}
                className="profile-pic"
              />
              <p>Welcome back to Book Social, {userDetails.username} !</p>
              <h1 className="name">{userProfiles.firstName}, {userProfiles.lastName}</h1>
              <p className="roles"> Authorization : {Array.isArray(userDetails.roles) &&
                  userDetails.roles.map(role => role.name).join(', ')}</p>
              <p className="dob">Date of Birth : {userProfiles.dob}</p>
              <p className="email">{userDetails.email}</p>
            </Box>
          </Card>
        </Box>
      ) : (
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            gap: "30px",
            justifyContent: "center",
            alignItems: "center",
            height: "100vh",
          }}
        >
          <CircularProgress></CircularProgress>
          <Typography>Loading ...</Typography>
        </Box>
      )}
    </>
  );
}
