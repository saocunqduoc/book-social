package com.nguyenvanlinh.identityservice.dto.response;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

// Trả về
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String email;
    boolean emailVerified;
    Set<RoleResponse> roles;
}
