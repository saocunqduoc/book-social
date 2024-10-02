package com.nguyenvanlinh.post.entity;

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
@Document(value = "like")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Like {
    @MongoId
    String id;

    String postId;
    String userId;
}
