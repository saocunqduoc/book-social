package com.nguyenvanlinh.identityservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyenvanlinh.identityservice.entity.EmailVerifyToken;

@Repository
public interface EmailVerifyTokenRepository extends JpaRepository<EmailVerifyToken, String> {
    Optional<EmailVerifyToken> findByUserIdAndToken(String userId, String token);
}
