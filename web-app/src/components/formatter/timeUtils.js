export const formatTime = (timestamp) => {
    const now = new Date();
    const commentTime = new Date(timestamp); // Chuyển đổi chuỗi ISO 8601 thành đối tượng Date
    const seconds = Math.floor((now - commentTime) / 1000);
    
    if (seconds < 60) return `${seconds} giây trước`;
    if (seconds < 3600) return `${Math.floor(seconds / 60)} phút trước`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)} giờ trước`;
    return `${Math.floor(seconds / 86400)} ngày trước`;
  };