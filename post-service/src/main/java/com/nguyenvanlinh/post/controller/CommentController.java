package com.nguyenvanlinh.post.controller;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.post.dto.response.ApiResponse;
import com.nguyenvanlinh.post.entity.Comment;
import com.nguyenvanlinh.post.service.CommentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{postId}/comments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;

    @PostMapping
    String commentOnPost(@PathVariable("postId") String postId, @RequestBody Comment comment) {
        commentService.commentOnPost(postId, comment);
        return comment.getUserId() + " comment to : " + postId;
    }

    @PutMapping("/{commentId}")
    ApiResponse<Void> updateComment(
            @PathVariable("postId") String postId,
            @PathVariable("commentId") String commentId,
            @RequestBody Comment updatedComment) {
        commentService.updateComment(postId, commentId, updatedComment);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/{commentId}")
    ApiResponse<Void> deleteComment(@PathVariable("postId") String postId, @PathVariable String commentId) {
        commentService.deleteComment(postId, commentId);
        return ApiResponse.<Void>builder().build();
    }
}
