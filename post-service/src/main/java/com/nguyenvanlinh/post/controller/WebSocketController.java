package com.nguyenvanlinh.post.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import com.nguyenvanlinh.post.entity.Comment;
import com.nguyenvanlinh.post.entity.Like;
import com.nguyenvanlinh.post.entity.Post;
import com.nguyenvanlinh.post.service.CommentService;
import com.nguyenvanlinh.post.service.LikeService;
import com.nguyenvanlinh.post.service.PostService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketController {

    LikeService likeService;
    PostService postService;
    CommentService commentService;

    @MessageMapping("/likes") // Đường dẫn mà client sẽ gửi yêu cầu đến
    @SendTo("/topic/like") // Đường dẫn mà client sẽ lắng nghe
    public Like likePost(Like like) {
        likeService.likePost(like.getPostId()); // Gọi service để xử lý like
        return like; // Trả về thông tin like để gửi đến client
    }

    @MessageMapping("/unlikes") // Đường dẫn cho unlike
    @SendTo("/topic/like") // Gửi thông báo đến cùng một topic
    public Like unlikePost(Like like) {
        likeService.unlikePost(like.getPostId()); // Gọi service để xử lý unlike
        return like; // Trả về thông tin like để gửi đến client
    }

    @MessageMapping("/comments") // Đường dẫn cho bình luận
    @SendTo("/topic/comment") // Gửi thông báo đến cùng một topic
    public Comment commentOnPost(Comment comment) {
        commentService.commentOnPost(comment.getPostId(), comment); // Gọi service để xử lý bình luận
        return comment; // Trả về thông tin bình luận để gửi đến client
    }

    @MessageMapping("/updateComment") // Đường dẫn cho cập nhật bình luận
    @SendTo("/topic/comment") // Gửi thông báo đến cùng một topic
    public Comment updateComment(Comment comment) {
        commentService.updateComment(
                comment.getPostId(), comment.getId(), comment); // Gọi service để cập nhật bình luận
        return comment; // Trả về thông tin bình luận đã cập nhật để gửi đến client
    }

    @MessageMapping("/deleteComment") // Đường dẫn cho xóa bình luận
    @SendTo("/topic/comment") // Gửi thông báo đến cùng một topic
    public String deleteComment(Comment comment) {
        commentService.deleteComment(comment.getPostId(), comment.getId()); // Gọi service để xóa bình luận
        return comment.getId(); // Trả về ID bình luận đã xóa để gửi đến client
    }

    @MessageMapping("/createPost") // Đường dẫn cho tạo bài viết
    @SendTo("/topic/post") // Gửi thông báo đến cùng một topic
    public Post createPost(Post post) { // Sử dụng PostRequest
        return postService.createPost(post); // Trả về thông tin bài viết đã tạo để gửi đến client
    }

    @MessageMapping("/updatePost") // Đường dẫn cho cập nhật bài viết
    @SendTo("/topic/post") // Gửi thông báo đến cùng một topic
    public Post updatePost(Post request) { // Sử dụng PostRequest
        return postService.updatePost(
                request.getId(), request); // Trả về thông tin bài viết đã cập nhật để gửi đến client
    }

    @MessageMapping("/deletePost") // Đường dẫn cho xóa bài viết
    @SendTo("/topic/post") // Gửi thông báo đến cùng một topic
    public String deletePost(Post post) {
        postService.deletePost(post.getId()); // Gọi service để xóa bài viết
        return post.getId(); // Trả về ID bài viết đã xóa để gửi đến client
    }
}
