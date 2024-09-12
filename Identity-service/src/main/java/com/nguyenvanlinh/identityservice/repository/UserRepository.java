package com.nguyenvanlinh.identityservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenvanlinh.identityservice.entity.User;

// JpaRepository< //Entity, //PrimaryKey type>
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Handle exception Username existed
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
// layer -> call user service with public method -> call repository to create User.
// we need to call below layer
