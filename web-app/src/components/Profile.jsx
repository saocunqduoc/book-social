import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import { formatTime } from './formatter/timeUtils'; // Import hàm formatTime
import { getToken, getLoginUserId, getUserId } from '../services/localStorageService';
import './css/Profile.css'; // Import file CSS mới
import EmojiPicker from 'emoji-picker-react';

const MyProfile = () => {

  const [posts, setPosts] = useState([]);
  const [newPost, setNewPost] = useState('');
  const [likes, setLikes] = useState({});
  const [comments, setComments] = useState({});
  const [likedPosts, setLikedPosts] = useState({});
  const [newComment, setNewComment] = useState({}); // Trạng thái cho ô nhập bình luận mới
  const [editingCommentContent, setEditingCommentContent] = useState(''); // Trạng thái cho nội dung bình luận đang chỉnh sửa
  const [editCommentId, setEditCommentId] = useState(null); // ID của bình luận đang chỉnh sửa
  const [dropdownCommentId, setDropdownCommentId] = useState(null); // ID của bình luận đang mở dropdown
  const commentInputRefs = useRef({});
  const socketRef = useRef(null); // Tham chiếu đến Socket.IO

    useEffect(() => {
      fetchPosts();
    }, []);

  const fetchPosts = async () => {
    const accessToken = getToken();
    // const loginUserId = getLoginUserId();
    const userId = getUserId();
    try {
        const response = await axios.get(`http://localhost:8668/api/v1/post/${userId}/posts`, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        });
        const postsData = response.data.result.data;
        const updatedLikedPosts = {};
        const updatedLikes = {};

        postsData.forEach(post => {
            updatedLikedPosts[post.id] = post.likes.some(like => like.userId === getLoginUserId());
            updatedLikes[post.id] = post.likes.length; // Cập nhật số lượt thích
        });

        setPosts(postsData);
        setLikedPosts(updatedLikedPosts);
        setLikes(updatedLikes); // Cập nhật trạng thái likes
        console.log(postsData);
    } catch (error) {
        console.error('Error fetching posts:', error);
    }
};

    useEffect(() => {
      socketRef.current = new WebSocket('ws://localhost:8084'); // Kết nối đến server WebSocket
  
      socketRef.current.onopen = () => {
          console.log('Connected to WebSocket');
      };
  
      socketRef.current.onmessage = (event) => {
          const message = JSON.parse(event.data);
          console.log('Message from server:', message);
  
          switch (message.type) {
              case 'like':
                  const likedPostId = message.data;
                  // Cập nhật trạng thái likes cho bài viết
                  setLikes((prevLikes) => ({
                      ...prevLikes,
                      [likedPostId.postId]: (prevLikes[likedPostId.postId] || 0) + 1,
                  }));
                  // Cập nhật trạng thái likedPosts cho bài viết chỉ nếu userId là người dùng hiện tại
                  if (likedPostId.userId === getLoginUserId()) {
                      setLikedPosts((prev) => ({ ...prev, [likedPostId.postId]: true }));
                  }
                  fetchPosts();
                  break;
              case 'unlike':
                  const unlikedPostId = message.data;
                  setLikes((prevLikes) => ({
                      ...prevLikes,
                      [unlikedPostId.postId]: Math.max((prevLikes[unlikedPostId.postId] || 1) - 1, 0),
                  }));
                  if ( unlikedPostId.userId === getLoginUserId()) {
                    setLikedPosts((prev) => ({ ...prev, [unlikedPostId.postId]: false }));
                  }
                  fetchPosts();
                  break;
              case 'comment':
                  const newCommentData = message.data;
                  setComments((prevComments) => ({
                      ...prevComments,
                      [newCommentData.postId]: [...(prevComments[newCommentData.postId] || []), newCommentData.content],
                  }));
                  fetchPosts();
                  break;
              case 'updateComment':
                  const updatedCommentData = message.data;
                  setComments((prevComments) => {
                      const postComments = prevComments[updatedCommentData.postId] || [];
                      return {
                          ...prevComments,
                          [updatedCommentData.postId]: postComments.map(comment =>
                              comment.id === updatedCommentData.commentId ? { ...comment, content: updatedCommentData.content } : comment
                          ),
                      };
                  });
                  fetchPosts();
                  break;
              case 'deleteComment':
                  const deletedCommentData = message.data;
                  setComments((prevComments) => {
                      const postComments = prevComments[deletedCommentData.postId] || [];
                      return {
                          ...prevComments,
                          [deletedCommentData.postId]: postComments.filter(comment => comment.id !== deletedCommentData.commentId),
                      };
                  });
                  fetchPosts();
                  break;
              case 'createPost':
                  const newPostData = message.data;
                  setNewPost((prevPosts) => [newPostData, ...prevPosts]);
                  setNewPost(''); // Reset ô nhập
                  fetchPosts();
                  break;
              case 'updatePost':
                  const updatedPostData = message.data;
                  setPosts((prevPosts) => {
                      return prevPosts.map(post => post.id === updatedPostData.id ? updatedPostData : post);
                  });
                  break;
              case 'deletePost':
                  const deletedPostId = message.data.id;
                  setPosts((prevPosts) => prevPosts.filter(post => post.id !== deletedPostId));
                  fetchPosts();
                  break;
              default:
                  console.log('Unknown message type:', message.type);
          }
      };
  
      socketRef.current.onerror = (error) => {
          console.error('WebSocket error:', error);
      };
  
      socketRef.current.onclose = () => {
          console.log('WebSocket connection closed');
      };
  
      return () => {
          socketRef.current.close();
      };
  }, []);

    const handleEditComment = (postId, commentId, content) => {
        setEditCommentId(commentId);
        setEditingCommentContent(content); // Đặt nội dung bình luận đang chỉnh sửa
    };

    const handleLikePost = async (postId) => {
      const accessToken = getToken();
      const userId = getLoginUserId();
      try {
          await axios.post(`http://localhost:8668/api/v1/post/${postId}/like`, {}, {
              headers: {
                  Authorization: `Bearer ${accessToken}`,
              },
          });
          socketRef.current.send(JSON.stringify({ type: 'like', data: { postId, userId } }));
      } catch (error) {
          console.error('Error liking post:', error);
      }
  };
  
  const handleUnlikePost = async (postId) => {
      const accessToken = getToken();
      const userId = getLoginUserId(); // Lấy userId từ localStorage hoặc context
      try {
          await axios.delete(`http://localhost:8668/api/v1/post/${postId}/like`, {
              headers: {
                  Authorization: `Bearer ${accessToken}`,
              },
          });
          socketRef.current.send(JSON.stringify({ type: 'unlike', data: { postId, userId } }));
      } catch (error) {
          console.error('Error unliking post:', error);
      }
  };

    const handleCommentSubmit = async (postId) => {
      if (newComment[postId]?.trim()) {
          const accessToken = getToken();
          try {
              await axios.post(`http://localhost:8668/api/v1/post/${postId}/comments`, {
                  content: newComment[postId],
              }, {
                  headers: {
                      Authorization: `Bearer ${accessToken}`,
                  },
              });
              // Cập nhật trạng thái comments
              setComments((prevComments) => ({
                  ...prevComments,
                  [postId]: [...(prevComments[postId] || []), newComment[postId]],
              }));
              setNewComment((prev) => ({ ...prev, [postId]: '' }));
              socketRef.current.send(JSON.stringify({ type: 'comment', data: { postId, content: newComment[postId] } }));
              } catch (error) {
                  console.error('Error posting comment:', error);
              }
          }
      };


      const handleUpdateComment = async (postId, commentId) => {
        const accessToken = getToken();
        if (editingCommentContent.trim()) {
        try {
            await axios.put(`http://localhost:8668/api/v1/post/${postId}/comments/${commentId}`, {
            content: editingCommentContent,
            }, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
            });
            setEditCommentId(null); // Đặt lại ID bình luận đang chỉnh sửa
            setEditingCommentContent('');
            socketRef.current.send(JSON.stringify({ type: 'updateComment', data: { postId, commentId, content: editingCommentContent } }));
        } catch (error) {
            console.error('Error updating comment:', error);
        }
        }
    };

    const handleDeleteComment = async (postId, commentId) => {
        const accessToken = getToken();
        try {
        await axios.delete(`http://localhost:8668/api/v1/post/${postId}/comments/${commentId}`, {
            headers: {
            Authorization: `Bearer ${accessToken}`,
            },
        });
        fetchPosts(); // Tải lại bài viết
        socketRef.current.send(JSON.stringify({ type: 'deleteComment', data: { postId, commentId } }));
        } catch (error) {
        console.error('Error deleting comment:', error);
        }
    };

    const handlePostSubmit = async () => {
        const accessToken = getToken();
        if (newPost.trim()) {
        try {
            await axios.post('http://localhost:8668/api/v1/post/create', { 
                content: newPost },
            {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
            });
            socketRef.current.send(JSON.stringify({ type: 'createPost', data: { content: newPost } }));
        } catch (error) {
            console.error('Error posting:', error);
        }
        }
    };

    const handleFocusCommentInput = (postId) => {
        // Focus vào ô nhập bình luận
        if (commentInputRefs.current[postId]) {
        commentInputRefs.current[postId].focus();
        }
    };
    const toggleDropdown = (commentId) => {
        // Nếu dropdown đang mở cho bình luận này, đóng nó
        if (dropdownCommentId === commentId) {
            setDropdownCommentId(null);
        } else {
            // Nếu không, mở dropdown cho bình luận này
            setDropdownCommentId(commentId);
        }
    };

    useEffect(() => {
        const handleKeyDown = (event) => {
            if (event.key === 'Escape') { // Kiểm tra nếu phím ESC được nhấn 
              setShowEmojiPickerEdit((prev) => ({ ...prev, [editCommentId]: false }))
              setEditCommentId(null); // Hủy chế độ chỉnh sửa
            }
        };
    
        document.addEventListener('keydown', handleKeyDown);
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, [editCommentId]);

    const [showEmojiPicker, setShowEmojiPicker] = useState({}); // Thay đổi trạng thái để quản lý từng post // Trạng thái hiển thị emoji picker
    const [showEmojiPickerEdit, setShowEmojiPickerEdit] = useState({}); // Trạng thái hiển thị emoji picker cho bình luận đang chỉnh sửa
    const handleEmojiClick = (emojiObject, postId) => {
        setNewComment((prev) => ({ ...prev, [postId]: (prev[postId] || '') + emojiObject.emoji })); // Thêm emoji vào ô nhập
        setShowEmojiPicker((prev) => ({ ...prev, [postId]: false })); // Đóng emoji picker sau khi chọn
    };
    const handleEmojiClickEdit = (emojiObject, postId) => {
      setEditingCommentContent((prev) => prev + emojiObject.emoji); // Thêm emoji vào nội dung đang chỉnh sửa
      setShowEmojiPickerEdit((prev) => ({ ...prev, [postId]: false })); // Đóng emoji picker sau khi chọn
    };

    const [dropdownPostId, setDropdownPostId] = useState(null); // Trạng thái để quản lý ID bài viết đang mở dropdown

    const handleToggleDropdown = (postId) => {
        setDropdownPostId(dropdownPostId === postId ? null : postId); // Mở hoặc đóng dropdown
    };

    const handleEditPost = async (postId) => {
      // Logic để sửa bài viết
      const updatedContent = prompt("Nhập nội dung mới cho bài viết:"); // Hoặc mở modal để nhập nội dung
      if (updatedContent) {
          try {
              await axios.put(`http://localhost:8668/api/v1/post/update/${postId}`, {
                  content: updatedContent,
                  headers: {
                    Authorization: `Bearer ${getToken()}`,
                  } // Giả sử bạn chỉ cập nhật nội dung
              });
              socketRef.current.send(JSON.stringify({ type: 'updatePost', data: { content: newPost } }));
          } catch (error) {
              console.error('Error updating post:', error);
          }
      }
  };

    const handleDeletePost = async (postId) => {
      const confirmDelete = window.confirm("Bạn có chắc chắn muốn xóa bài viết này không?");
      if (confirmDelete) {
          try {
              await axios.delete(`http://localhost:8668/api/v1/post/delete/${postId}`,{
                  headers: {
                    Authorization: `Bearer ${getToken()}`,
                  } 
                }
              );
          
              socketRef.current.send(JSON.stringify({ type: 'deletePost', data: { content: newPost } }));
          } catch (error) {
              console.error('Error deleting post:', error);
          }
      }
  };
  return (
    <div className="profile-container">
      {getUserId() === getLoginUserId() && ( // Kiểm tra điều kiện
                <div className="new-post">
                    <div className="post-input">
                        <textarea
                            value={newPost}
                            onChange={(e) => setNewPost(e.target.value)}
                            placeholder="Bạn đang nghĩ gì?"
                            rows="3"
                        />
                        <button onClick={handlePostSubmit}>Đăng</button>
                    </div>
                </div>
            )}
        <div className="posts">
          {posts.map((post) => (
            <div key={post.id} className="post-card">
              <div className="post-header">
                <img src={post.avatar || "https://static-00.iconduck.com/assets.00/avatar-default-icon-2048x2048-h6w375ur.png"} alt="Avatar" className="avatar" />
                <div className="post-info">
                  <h3>{post.username}</h3>
                  <p className="post-date">{post.created}</p>
                </div>
                <button onClick={() => handleToggleDropdown(post.id)}>...</button> {/* Nút để mở dropdown */}
                        {dropdownPostId === post.id && ( // Hiển thị dropdown nếu ID bài viết trùng khớp
                            <div className="dropdown-menu">
                                <button onClick={() => handleEditPost(post.id)}>Chỉnh sửa</button>
                                <button onClick={() => handleDeletePost(post.id)}>Xóa</button>
                            </div>
                        )}
              </div>
              <p className="post-content">
                {post.content.split('\n').map((line, index) => (
                  <span key={index}>
                    {line}
                    <br />
                  </span>
              ))}
            </p>
            <span className='like-count'>{post.likes.length > 0 ? post.likes.length + ' lượt thích' : ''}</span>
            <span className='comment-count'>{post.comments.length ? post.comments.length + ' bình luận' : ''}</span>
            <div className="divider"></div>
            <div className="post-actions">
              {likedPosts[post.id] ? (
                <button onClick={() => handleUnlikePost(post.id)}>Bỏ thích</button>
              ) : (
                <button onClick={() => handleLikePost(post.id)}>Thích</button>
              )}
              <div className="divider vertical"></div>
              <button onClick={() => handleFocusCommentInput(post.id)}>Bình luận</button>
            </div>
            <div className="divider"></div>
            <div className="comment-input">
              <img src={post.avatar || "https://static-00.iconduck.com/assets.00/avatar-default-icon-2048x2048-h6w375ur.png"} alt="Avatar" className="avatar" />
              <textarea
                ref={(el) => (commentInputRefs.current[post.id] = el)}
                value={newComment[post.id] || ''}
                onChange={(e) => setNewComment((prev) => ({ ...prev, [post.id]: e.target.value }))}
                placeholder="Viết bình luận mới..."
                rows="2"
              />
              {showEmojiPicker[post.id] && ( // Hiển thị emoji picker chỉ cho post hiện tại
                  <div className="emoji-picker-container"> {/* Sử dụng lớp CSS mới */}
                      <EmojiPicker onEmojiClick={(emojiObject) => handleEmojiClick(emojiObject, post.id)} /> {/* Hiển thị emoji picker */}
                  </div>
              )}
              <button onClick={() => setShowEmojiPicker((prev) => ({ ...prev, [post.id]: !prev[post.id] }))}>😊</button> {/* Nút để mở emoji picker */}
              <button onClick={() => handleCommentSubmit(post.id)}>
                Gửi
              </button>
            </div>
            <div className="comments">
              {post.comments && post.comments.slice().reverse().map((comment) => (
                <div key={comment.id} className="comment">
                  <div className="divider"></div>
                  <div className="comment-header">
                    <img src={post.avatar || "https://static-00.iconduck.com/assets.00/avatar-default-icon-2048x2048-h6w375ur.png"} alt="Avatar" className="avatar" />
                    <div className="post-info">
                      <h3>{comment.username}</h3>
                      <p className="post-date">{comment.commentDate && !comment.modifiedDate ? (
                            formatTime(comment.commentDate) // Hiển thị thời gian tạo nếu là bình luận mới
                        ) : (
                            comment.modifiedDate ? (
                                `Đã chỉnh sửa - ${formatTime(comment.modifiedDate)} ` // Hiển thị thời gian chỉnh sửa
                            ) : (
                                formatTime(comment.commentDate) // Nếu không có thời gian chỉnh sửa, hiển thị thời gian tạo
                            )
                        )}</p>
                    </div>
                    {comment.userId === getLoginUserId() && ( // Kiểm tra nếu bình luận là của người dùng hiện tại
                        <div className="comment-actions">
                            <button className="dropdown-button" onClick={() => toggleDropdown(comment.id)}>...</button>
                            {dropdownCommentId === comment.id && (
                                <div className="dropdown-menu">
                                    <button onClick={() => handleEditComment(post.id, comment.id, comment.content)}>Chỉnh sửa</button>
                                    <button onClick={() => handleDeleteComment(post.id, comment.id)}>Xóa</button>
                                </div>
                            )}
                        </div>
                    )}
                  </div>
                  {editCommentId === comment.id ? ( // Hiển thị ô nhập để chỉnh sửa
                      <div className="comment-update-container">
                          <textarea
                              value={editingCommentContent} // Sử dụng nội dung đang chỉnh sửa
                              onChange={(e) => setEditingCommentContent(e.target.value)} // Cập nhật nội dung đang chỉnh sửa
                          />
                          <button onClick={() => setShowEmojiPickerEdit((prev) => ({ ...prev, [comment.id]: !prev[comment.id] }))}>😊</button>
                          <button onClick={
                            () => { handleUpdateComment(post.id, comment.id); 
                                    setShowEmojiPickerEdit((prev) => ({ ...prev, [comment.id]: false })) }}
                          >Cập nhật</button>
                          <button onClick={() => { setEditCommentId(null); 
                                                    setEditingCommentContent(''); 
                                                    setShowEmojiPickerEdit((prev) => ({ ...prev, [comment.id]: false })) }}
                          >Hủy</button> {/* Nút hủy để thoát khỏi chế độ chỉnh sửa */}
                      </div>
                  ) : (
                      <p className="comment-content">{comment.content}</p>
                  )}
                  {showEmojiPickerEdit[comment.id] && ( // Hiển thị emoji picker chỉ cho bình luận đang chỉnh sửa
                      <div>
                          <EmojiPicker onEmojiClick={(emojiObject) => handleEmojiClickEdit(emojiObject, post.id)} /> {/* Hiển thị emoji picker */}
                      </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MyProfile;