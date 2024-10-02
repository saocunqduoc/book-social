package com.nguyenvanlinh.post.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.nguyenvanlinh.post.entity.Comment;

public interface CommentRepository extends MongoRepository<Comment, String> {}
