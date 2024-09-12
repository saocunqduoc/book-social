package com.nguyenvanlinh.profile.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.nguyenvanlinh.profile.dto.request.ProfileCreationRequest;
import com.nguyenvanlinh.profile.dto.request.ProfileUpdateRequest;
import com.nguyenvanlinh.profile.dto.response.UserProfileResponse;
import com.nguyenvanlinh.profile.entity.UserProfile;

@Mapper(componentModel = "spring") // notify that mapper is a bean
public interface UserProfileMapper {
    UserProfile toUserProfile(ProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    void updateProfile(@MappingTarget UserProfile userProfile, ProfileUpdateRequest request);
}
