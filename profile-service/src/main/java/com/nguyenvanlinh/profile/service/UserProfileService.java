package com.nguyenvanlinh.profile.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nguyenvanlinh.profile.dto.request.ProfileCreationRequest;
import com.nguyenvanlinh.profile.dto.request.ProfileUpdateRequest;
import com.nguyenvanlinh.profile.dto.response.UserProfileResponse;
import com.nguyenvanlinh.profile.entity.UserProfile;
import com.nguyenvanlinh.profile.exception.AppException;
import com.nguyenvanlinh.profile.exception.ErrorCode;
import com.nguyenvanlinh.profile.mapper.UserProfileMapper;
import com.nguyenvanlinh.profile.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileService {

    UserProfileRepository userProfileRepository;

    UserProfileMapper userProfileMapper;

    // Create new
    public UserProfileResponse createProfile(ProfileCreationRequest request) {
        // use mapper and pass param to create new profile
        UserProfile userProfile = userProfileMapper.toUserProfile(request);
        // save new profile to repository
        userProfile = userProfileRepository.save(userProfile);
        // return Response of Profile
        return userProfileMapper.toUserProfileResponse(userProfile);
    }
    // get my Profile
    public UserProfileResponse getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        var profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userProfileMapper.toUserProfileResponse(profile);
    }
    // get By Id
    public UserProfileResponse getProfile(String id) {
        UserProfile userProfile =
                userProfileRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }
    // get User Profile
    public UserProfileResponse getByUserId(String userId) {
        UserProfile userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }
    // getAll
    @PreAuthorize("hasRole('ADMIN')")
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
        UserProfile userProfile = userProfileRepository
                .findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_EXISTED));
        userProfileMapper.updateProfile(userProfile, request);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }
}
