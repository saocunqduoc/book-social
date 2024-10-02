import { removeToken, removeUserId, removeLoginUserId, getToken } from "./localStorageService";

export const logOut = () => {
  removeToken();
  removeUserId();
  removeLoginUserId();
};

export const isAuthenticated = () => {
  return getToken();
};

