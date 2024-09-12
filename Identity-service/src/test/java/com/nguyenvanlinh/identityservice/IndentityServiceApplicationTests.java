package com.nguyenvanlinh.identityservice;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class IdentityServiceApplicationTests {

    @Test
    void hash() throws NoSuchAlgorithmException {
        String password = "123456";

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());

        byte[] digest = md.digest();
        String md5Hash = DatatypeConverter.printHexBinary(digest);
        log.info("md5Hash-1 : {}", md5Hash);

        md.update(password.getBytes());
        digest = md.digest();
        md5Hash = DatatypeConverter.printHexBinary(digest);

        log.info("md5Hash-2 : {}", md5Hash);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        // $2a$10$fw4WzmnTM6mIqj2QjP5Vp.F1/7iFJ8cOQw.WkcJSWuivXCrvEjJOi
        // $2a -> version của thuật toán
        // $10 -> độ phức tạp của thuật toán
        // $fw4WzmnTM6mIqj2QjP5Vp. -> Salt được gen ngẫu nghiên (22 ký tự)
        // F1/7iFJ8cOQw.WkcJSWuivXCrvEjJOi -> hash (31 ký tự)
        log.info("passwordEncoder-1 : {}", passwordEncoder.encode(password));
        log.info("passwordEncoder-2 : {}", passwordEncoder.encode(password));
    }
}
