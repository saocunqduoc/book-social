package com.nguyenvanlinh.post.dto.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.nguyenvanlinh.post.entity.Comment;
import com.nguyenvanlinh.post.entity.Like;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String content;
    String userId;
    String username;
    String firstName;
    String lastName;
    String created;
    Instant createdDate;
    Instant modifiedDate;

    @Builder.Default
    List<Like> likes = new ArrayList<>();

    @Builder.Default
    List<Comment> comments = new ArrayList<>();
}
