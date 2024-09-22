package com.nguyenvanlinh.post.controller;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.post.dto.request.PostRequest;
import com.nguyenvanlinh.post.dto.response.ApiResponse;
import com.nguyenvanlinh.post.dto.response.PageResponse;
import com.nguyenvanlinh.post.dto.response.PostResponse;
import com.nguyenvanlinh.post.service.PostService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    PostService postService;

    @PostMapping("/create")
    ApiResponse<PostResponse> createPost(@RequestBody PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request))
                .build();
    }

    @GetMapping("/my-posts")
    ApiResponse<PageResponse<PostResponse>> getMyPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getMyPosts(page, size))
                .build();
    }
}
