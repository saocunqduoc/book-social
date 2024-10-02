package com.nguyenvanlinh.post.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nguyenvanlinh.post.entity.Like;
import com.nguyenvanlinh.post.entity.Post;
import com.nguyenvanlinh.post.exception.AppException;
import com.nguyenvanlinh.post.exception.ErrorCode;
import com.nguyenvanlinh.post.repository.LikeRepository;
import com.nguyenvanlinh.post.repository.PostRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeService {

    PostRepository postRepository;
    LikeRepository likeRepository;

    public void likePost(String postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        // Kiểm tra xem người dùng đã like bài viết chưa
        if (post.getLikes().stream().anyMatch(like -> like.getUserId().equals(userId))) {
            throw new AppException(ErrorCode.ALREADY_LIKE_THIS_POST);
        }

        // Thêm like mới
        Like like = Like.builder().userId(userId).postId(postId).build();
        post.getLikes().add(like);
        likeRepository.save(like);
        postRepository.save(post);
    }

    public void unlikePost(String postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Tìm và xóa like từ LikeRepository
        Like likeToRemove = likeRepository.findByPostIdAndUserId(postId, userId);
        if (likeToRemove != null) {
            likeRepository.delete(likeToRemove); // Xóa like khỏi LikeRepository
        }

        // Xóa like khỏi danh sách likes trong Post
        post.getLikes().removeIf(like -> like.getUserId().equals(userId));
        postRepository.save(post); // Lưu lại Post đã cập nhật
    }

    public List<Like> getLikesForPost(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        return post.getLikes();
    }
}
