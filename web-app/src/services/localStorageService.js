export const KEY_TOKEN = "accessToken";
export const KEY_USERID = "userId";
export const KEY_LOGIN_USERID = "loginUserId";

// Login userId 
export const setLoginUserId = (userId) => {
  localStorage.setItem(KEY_LOGIN_USERID, userId);
};

export const getLoginUserId = () => {
  return localStorage.getItem(KEY_LOGIN_USERID);
};

export const removeLoginUserId = () => {
  return localStorage.removeItem(KEY_LOGIN_USERID);
};
// Token
export const setToken = (token) => {
  localStorage.setItem(KEY_TOKEN, token);
};

export const getToken = () => {
  return localStorage.getItem(KEY_TOKEN);
};

export const removeToken = () => {
  return localStorage.removeItem(KEY_TOKEN);
};
// UserId
export const setUserId = (userId) => {
  localStorage.setItem(KEY_USERID, userId);
}
export const getUserId = () => {
  return localStorage.getItem(KEY_USERID);
}
export const removeUserId = () => {
  return localStorage.removeItem("userId");
}
