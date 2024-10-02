package com.nguyenvanlinh.profile.service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.nguyenvanlinh.profile.dto.response.FriendResponse;
import com.nguyenvanlinh.profile.dto.response.ProfileFriendResponse;
import com.nguyenvanlinh.profile.entity.Friend;
import com.nguyenvanlinh.profile.entity.UserProfile;
import com.nguyenvanlinh.profile.exception.AppException;
import com.nguyenvanlinh.profile.exception.ErrorCode;
import com.nguyenvanlinh.profile.repository.FriendRepository;
import com.nguyenvanlinh.profile.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendService {
    FriendRepository friendRepository;
    UserProfileRepository userProfileRepository;

    // Gửi yêu cầu kết bạn
    public void sendFriendRequest(String friendId, Authentication authentication) {
        String userId = authentication.getName(); // Lấy userId từ JWT

        // Kiểm tra xem người dùng và bạn bè có tồn tại không
        var userOpt = userProfileRepository.findByUserId(userId);
        var friendOpt = userProfileRepository.findByUserId(friendId);

        if (userOpt.isPresent() && friendOpt.isPresent()) {
            // Kiểm tra xem đã có mối quan hệ bạn bè chưa
            if (friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent()) {
                throw new AppException(ErrorCode.ALREADY_IN_FRIENDSHIP);
            }

            // Tạo yêu cầu kết bạn
            Friend friendRequest = Friend.builder()
                    .userId(userId)
                    .friendId(friendId)
                    .requestSentAt(Instant.now())
                    .isFriend(false) // Chưa chấp nhận
                    .build();
            friendRepository.save(friendRequest);
        }
    }

    // Chấp nhận yêu cầu kết bạn
    public void acceptFriendRequest(String friendId, Authentication authentication) {
        String userId = authentication.getName(); // Lấy userId từ JWT

        // Tìm yêu cầu kết bạn
        Optional<Friend> friendRequestOpt = friendRepository.findByUserIdAndFriendId(friendId, userId);

        if (friendRequestOpt.isPresent()) {
            Friend friendRequest = friendRequestOpt.get();
            friendRequest.setRequestAcceptedAt(Instant.now());
            friendRequest.setFriend(true); // Đánh dấu là bạn bè
            friendRepository.save(friendRequest);

            // Tạo một mối quan hệ bạn bè
            Friend newFriend = new Friend(userId, friendId, Instant.now(), Instant.now(), true);
            friendRepository.save(newFriend);

            // Cập nhật danh sách bạn bè cho cả hai người dùng
            UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
            UserProfile friendProfile =
                    userProfileRepository.findByUserId(friendId).orElse(null);
            if (userProfile != null) {
                // Cập nhật danh sách bạn bè của người chấp nhận yêu cầu
                userProfile.getFriends().add(newFriend);
                userProfileRepository.save(userProfile);
            }

            if (friendProfile != null) {
                // Cập nhật danh sách bạn bè của người gửi yêu cầu
                friendProfile.getFriends().add(newFriend);
                userProfileRepository.save(friendProfile);
            }
        }
    }

    // Hủy kết bạn
    public void unfriend(String friendId, Authentication authentication) {
        String userId = authentication.getName(); // Lấy userId từ JWT
        // Xóa mối quan hệ bạn bè
        friendRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendRepository.deleteByUserIdAndFriendId(friendId, userId);
    }

    // Lấy danh sách bạn bè
    public ProfileFriendResponse getFriends(Authentication authentication) {
        String userId = authentication.getName(); // Lấy userId từ JWT
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);

        if (userProfile != null) {
            Set<FriendResponse> friends = friendRepository.findByUserId(userId).stream()
                    .filter(Friend::isFriend) // Chỉ lấy những người bạn đã chấp nhận
                    .map(friend -> {
                        UserProfile friendProfile = userProfileRepository
                                .findByUserId(friend.getFriendId())
                                .orElse(null);
                        return new FriendResponse(
                                Objects.requireNonNull(friendProfile).getUsername(),
                                friend.getFriendId(),
                                friendProfile.getFirstName(),
                                friendProfile.getLastName(),
                                friendProfile.getEmail());
                    })
                    .collect(Collectors.toSet());

            return new ProfileFriendResponse(
                    userProfile.getId(),
                    userProfile.getUserId(),
                    userProfile.getUsername(),
                    userProfile.getEmail(),
                    userProfile.getFirstName(),
                    userProfile.getLastName(),
                    userProfile.getDob(),
                    userProfile.getCity(),
                    friends);
        }
        return null;
    }
}
