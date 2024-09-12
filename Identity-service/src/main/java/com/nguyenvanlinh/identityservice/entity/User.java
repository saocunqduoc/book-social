package com.nguyenvanlinh.identityservice.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
// Tạo đầu tiên
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    // unique field -> request fast will got this -> don't have duplicate username when request = time
    @Column(
            name = "username",
            unique = true,
            columnDefinition = "VARCHAR(255) collate utf8mb4_unicode_ci") // không phân biet chữ hoa hay chu thường
    String username;

    String password;
    String firstName;
    String lastName;
    LocalDate dob;
    // Set là kểu lưu trữ (các phần tử )value unique
    @ManyToMany
    Set<Role> roles;

    // Generate getter/ setter để Hibernates co thể map data
}
