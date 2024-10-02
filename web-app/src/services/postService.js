import { removeToken, removeUserId, getToken } from "./localStorageService";

export const createPost = async (content) => {
  return await fetch(`http://localhost:8080/api/posts/create`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
      body: JSON.stringify(content),
    });
};
