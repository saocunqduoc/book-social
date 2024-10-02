package com.nguyenvanlinh.post.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nguyenvanlinh.post.dto.response.PageResponse;
import com.nguyenvanlinh.post.dto.response.PostResponse;
import com.nguyenvanlinh.post.dto.response.UserProfileResponse;
import com.nguyenvanlinh.post.entity.Comment;
import com.nguyenvanlinh.post.entity.Like;
import com.nguyenvanlinh.post.entity.Post;
import com.nguyenvanlinh.post.exception.AppException;
import com.nguyenvanlinh.post.exception.ErrorCode;
import com.nguyenvanlinh.post.mapper.PostMapper;
import com.nguyenvanlinh.post.repository.PostRepository;
import com.nguyenvanlinh.post.repository.httpclient.ProfileClient;
import com.nguyenvanlinh.post.service.Fomat.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    PostRepository postRepository;
    PostMapper postMapper;
    LikeService likeService;
    CommentService commentService;

    DateTimeFormatter dateTimeFormatter;
    ProfileClient profileClient;

    public Post createPost(Post request) {
        // get User Id hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Post post = Post.builder()
                .content(request.getContent())
                .userId(authentication.getName()) // getName() ở JWT identity-service là getUserId
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();
        post = postRepository.save(post);
        return post;
    }

    public Post updatePost(String postId, Post request) {
        Post existingPost =
                postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Cập nhật các thuộc tính của bài viết
        existingPost.setContent(request.getContent());
        existingPost.setModifiedDate(Instant.now()); // Cập nhật thời gian sửa
        if (existingPost.getContent().isEmpty()) {
            postRepository.delete(existingPost);
        }
        // Lưu bài viết đã cập nhật
        return postRepository.save(existingPost);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream().map(postMapper::toPostResponse).toList();
    }

    public Void deletePost(String postId) {
        var post = postRepository.findPostById(postId);
        postRepository.delete(post);
        return null;
    }

    public Post getPost(String postId) {
        return postRepository.findPostById(postId);
    }

    public PageResponse<PostResponse> getPostsByUserId(String userId, int page, int size) {
        UserProfileResponse userProfile = null;
        try {
            userProfile = profileClient.getUserProfile(userId).getResult();
        } catch (Exception e) {
            log.error("Error while getting user profile", e);
        }

        // Pagination
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate"); // sort theo createDate của entity
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        var pageData = postRepository.findAllByUserId(userId, pageable);

        String username = userProfile != null ? userProfile.getUsername() : null;
        String firstName = userProfile != null ? userProfile.getFirstName() : null;
        String lastName = userProfile != null ? userProfile.getLastName() : null;

        var pageList = pageData.getContent().stream()
                .map(post -> {
                    var postResponse = postMapper.toPostResponse(post);
                    postResponse.setCreated(dateTimeFormatter.format(post.getCreatedDate()));
                    postResponse.setUsername(username);
                    postResponse.setFirstName(firstName);
                    postResponse.setLastName(lastName);

                    // Lấy danh sách likes và comments
                    List<Like> likes = likeService.getLikesForPost(post.getId()); // Giả sử bạn có phương thức này
                    List<Comment> comments =
                            commentService.getCommentsForPost(post.getId()); // Giả sử bạn có phương thức này

                    postResponse.setLikes(likes); // Thêm danh sách likes vào postResponse
                    postResponse.setComments(comments); // Thêm danh sách comments vào postResponse
                    return postResponse;
                })
                .toList();

        return PageResponse.<PostResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(pageList)
                .build();
    }

    //    public PageResponse<PostResponse> getMyPosts(int page, int size) {
    //        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //        String userId = authentication.getName();
    //
    //        UserProfileResponse userProfile = null;
    //        try {
    //            userProfile = profileClient.getUserProfile(userId).getResult();
    //        } catch (Exception e) {
    //            log.error("Error while getting user profile", e);
    //        }
    //        // Pagination
    //        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate"); // sort theo createDate của entity
    //        Pageable pageable = PageRequest.of(page - 1, size, sort);
    //        var pageData = postRepository.findAllByUserId(userId, pageable);
    //
    //        String username = userProfile != null ? userProfile.getUsername() : null;
    //        String firstName = userProfile != null ? userProfile.getFirstName() : null;
    //        String lastName = userProfile != null ? userProfile.getLastName() : null;
    //
    //        var pageList = pageData.getContent().stream()
    //                .map(post -> {
    //                    var postResponse = postMapper.toPostResponse(post);
    //                    postResponse.setCreated(dateTimeFormatter.format(post.getCreatedDate()));
    //                    postResponse.setUsername(username);
    //                    postResponse.setFirstName(firstName);
    //                    postResponse.setLastName(lastName);
    //                    // Lấy danh sách likes và comments
    //                    List<Like> likes = likeService.getLikesForPost(post.getId()); // Giả sử bạn có phương thức này
    //                    List<Comment> comments = commentService.getCommentsForPost(post.getId()); // Giả sử bạn có
    // phương thức này
    //
    //                    postResponse.setLikes(likes); // Thêm danh sách likes vào postResponse
    //                    postResponse.setComments(comments); // Thêm danh sách comments vào postResponse
    //                    return postResponse;
    //                })
    //                .toList();
    //
    //        return PageResponse.<PostResponse>builder()
    //                .currentPage(page)
    //                .pageSize(pageData.getSize())
    //                .totalPages(pageData.getTotalPages())
    //                .totalElements(pageData.getTotalElements())
    //                .data(pageList)
    //                .build();
    //    }
}
