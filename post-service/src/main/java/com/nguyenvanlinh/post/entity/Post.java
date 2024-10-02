package com.nguyenvanlinh.post.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
@Document(value = "post")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @MongoId
    String id;

    String userId; // ID của người đăng
    String content;
    String username;
    String firstName;
    String lastName;
    // ??? Có nên thêm username và LastName + FirstName không?
    // Vì sao phải xài profileClient với OpenFeign

    @Builder.Default
    List<Like> likes = new ArrayList<>(); // Danh sách ID người đã like

    @Builder.Default
    List<Comment> comments = new ArrayList<>(); // Danh sách comment

    String created; // để format
    Instant createdDate; // Thời gian đăng
    Instant modifiedDate; // Thời gian sửa
}
