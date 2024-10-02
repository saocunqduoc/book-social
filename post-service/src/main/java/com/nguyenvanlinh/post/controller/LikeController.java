package com.nguyenvanlinh.post.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.post.dto.response.ApiResponse;
import com.nguyenvanlinh.post.entity.Like;
import com.nguyenvanlinh.post.service.LikeService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{postId}/like")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeController {
    LikeService likeService;

    @PostMapping
    ApiResponse<Void> likePost(@PathVariable String postId) {
        likeService.likePost(postId);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping
    ApiResponse<Void> unlikePost(@PathVariable String postId) {
        likeService.unlikePost(postId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping
    public ApiResponse<List<Like>> getLikesForPost(@PathVariable String postId) {
        List<Like> likes = likeService.getLikesForPost(postId);
        return ApiResponse.<List<Like>>builder().result(likes).build();
    }
}
