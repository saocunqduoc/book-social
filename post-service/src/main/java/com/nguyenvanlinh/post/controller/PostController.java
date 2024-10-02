package com.nguyenvanlinh.post.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.post.dto.response.ApiResponse;
import com.nguyenvanlinh.post.dto.response.PageResponse;
import com.nguyenvanlinh.post.dto.response.PostResponse;
import com.nguyenvanlinh.post.entity.Post;
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
    ApiResponse<Post> createPost(@RequestBody Post request) {
        return ApiResponse.<Post>builder()
                .result(postService.createPost(request))
                .build();
    }

    @PutMapping("/update/{postId}")
    ApiResponse<Post> updatePost(@PathVariable("postId") String postId, @RequestBody Post request) {
        return ApiResponse.<Post>builder()
                .result(postService.updatePost(postId, request))
                .build();
    }

    @DeleteMapping("/delete/{postId}")
    ApiResponse<Void> deletePost(@PathVariable("postId") String postId) {
        return ApiResponse.<Void>builder()
                .result(postService.deletePost(postId))
                .build();
    }

    @GetMapping("/posts")
    ApiResponse<List<PostResponse>> getAllPosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .result(postService.getAllPosts())
                .build();
    }

    @GetMapping("/{postId}")
    Post getPost(@PathVariable("postId") String postId) {
        return postService.getPost(postId);
    }

    @GetMapping("/{userId}/posts")
    public ApiResponse<PageResponse<PostResponse>> getPostsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        PageResponse<PostResponse> posts = postService.getPostsByUserId(userId, page, size);
        return ApiResponse.<PageResponse<PostResponse>>builder().result(posts).build();
    }
}
