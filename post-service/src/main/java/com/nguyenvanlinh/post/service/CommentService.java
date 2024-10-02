package com.nguyenvanlinh.post.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nguyenvanlinh.post.dto.response.UserProfileResponse;
import com.nguyenvanlinh.post.entity.Comment;
import com.nguyenvanlinh.post.entity.Post;
import com.nguyenvanlinh.post.exception.AppException;
import com.nguyenvanlinh.post.exception.ErrorCode;
import com.nguyenvanlinh.post.repository.CommentRepository;
import com.nguyenvanlinh.post.repository.PostRepository;
import com.nguyenvanlinh.post.repository.httpclient.ProfileClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    PostRepository postRepository;
    CommentRepository commentRepository;

    ProfileClient profileClient;

    public void commentOnPost(String postId, Comment comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        UserProfileResponse userProfile = null;
        try {
            userProfile = profileClient.getUserProfile(userId).getResult();
        } catch (Exception e) {
            log.error("Error while getting user profile", e);
        }
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        String username = userProfile != null ? userProfile.getUsername() : null;

        // Thiết lập thông tin cho comment
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setPostId(postId);
        comment.setCommentDate(Instant.now());

        // Thêm comment vào danh sách comments
        post.getComments().add(comment);

        // Lưu comment vào cơ sở dữ liệu
        commentRepository.save(comment);

        // Lưu post với comment mới
        postRepository.save(post);
    }

    public void updateComment(String postId, String commentId, Comment updatedComment) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Tìm và cập nhật comment
        post.getComments().stream()
                .filter(comment -> comment.getId().equals(commentId))
                .findFirst()
                .ifPresent(comment -> {
                    comment.setContent(updatedComment.getContent());
                    comment.setModifiedDate(Instant.now());
                    commentRepository.save(comment);
                });

        if (updatedComment.getContent().isEmpty()) {
            deleteComment(postId, commentId);
        }
        postRepository.save(post);
    }

    public void deleteComment(String postId, String commentId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        Comment commentToRemove = commentRepository.findById(commentId).orElse(null);
        if (commentToRemove != null) {
            commentRepository.delete(commentToRemove);
        }
        // Xóa comment
        post.getComments().removeIf(comment -> comment.getId().equals(commentId));
        postRepository.save(post);
    }

    public List<Comment> getCommentsForPost(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        return post.getComments(); // Trả về danh sách bình luận của bài viết
    }
}
