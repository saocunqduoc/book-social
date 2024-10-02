package com.nguyenvanlinh.profile.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendResponse {
    String username; // Thay đổi từ userName thành username cho nhất quán
    String friendId;
    String firstName;
    String lastName;
    String email;
}
