package com.nguyenvanlinh.post.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.nguyenvanlinh.post.entity.Like;

public interface LikeRepository extends MongoRepository<Like, String> {
    Like findByPostIdAndUserId(String postId, String userId);
}
