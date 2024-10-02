package com.nguyenvanlinh.post.entity;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@Document(value = "comment")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @MongoId
    String id;

    String postId;
    String userId; // ID người bình luận
    String username;
    String content; // Nội dung bình luận

    Instant commentDate;
    Instant modifiedDate;
}
