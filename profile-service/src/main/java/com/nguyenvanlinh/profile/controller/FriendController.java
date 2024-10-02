package com.nguyenvanlinh.profile.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.profile.dto.response.ProfileFriendResponse;
import com.nguyenvanlinh.profile.service.FriendService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping({"/friends", "/friends/"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendController {

    FriendService friendService;

    // Gửi yêu cầu kết bạn
    @PostMapping("/request/{friendId}")
    String sendFriendRequest(@PathVariable String friendId, Authentication authentication) {
        friendService.sendFriendRequest(friendId, authentication);
        return "Friend request sent from " + authentication.getName() + "to " + friendId;
    }

    // Chấp nhận yêu cầu kết bạn
    @PostMapping("/accept/{friendId}")
    String acceptFriendRequest(@PathVariable String friendId, Authentication authentication) {
        friendService.acceptFriendRequest(friendId, authentication);
        return authentication.getName() + " accepted friend request from " + friendId;
    }

    // Hủy kết bạn
    @DeleteMapping("/unfriend/{friendId}")
    String unfriend(@PathVariable String friendId, Authentication authentication) {
        friendService.unfriend(friendId, authentication);
        return authentication.getName() + " unfriend with " + friendId;
    }

    // Lấy danh sách bạn bè
    @GetMapping
    ProfileFriendResponse getFriends(Authentication authentication) {
        return friendService.getFriends(authentication);
    }
}
