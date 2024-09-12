package com.nguyenvanlinh.profile.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nguyenvanlinh.profile.dto.request.ProfileCreationRequest;
import com.nguyenvanlinh.profile.dto.request.ProfileUpdateRequest;
import com.nguyenvanlinh.profile.dto.response.UserProfileResponse;
import com.nguyenvanlinh.profile.entity.UserProfile;
import com.nguyenvanlinh.profile.mapper.UserProfileMapper;
import com.nguyenvanlinh.profile.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;

    // Create new
    public UserProfileResponse createProfile(ProfileCreationRequest request) {
        UserProfile userProfile =
                userProfileMapper.toUserProfile(request); // use mapper and pass param to create new profile
        userProfile = userProfileRepository.save(userProfile); // save new profile to repository
        return userProfileMapper.toUserProfileResponse(userProfile); // return Response of Profile
    }
    // get By Id
    public UserProfileResponse getProfile(String id) {
        UserProfile userProfile =
                userProfileRepository.findById(id).orElseThrow(() -> new RuntimeException("Profile not found"));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }
    // getAll
    public List<UserProfileResponse> getAllProfiles() {
        return userProfileRepository.findAll().stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }
    // Delete
    public void deleteProfile(String profileId) {
        userProfileRepository.deleteById(profileId);
    }
    // Update
    public UserProfileResponse updateProfile(String profileId, ProfileUpdateRequest request) {
        UserProfile userProfile =
                userProfileRepository.findById(profileId).orElseThrow(() -> new RuntimeException("Profile not found"));
        userProfileMapper.updateProfile(userProfile, request);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }
}
