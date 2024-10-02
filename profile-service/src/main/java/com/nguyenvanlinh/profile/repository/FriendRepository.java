package com.nguyenvanlinh.profile.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.nguyenvanlinh.profile.entity.Friend;

public interface FriendRepository extends Neo4jRepository<Friend, String> {
    List<Friend> findByUserId(String userId);

    List<Friend> findByFriendId(String friendId);

    Optional<Friend> findByUserIdAndFriendId(String userId, String friendId);

    void deleteByUserIdAndFriendId(String userId, String friendId);
}
