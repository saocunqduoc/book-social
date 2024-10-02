package com.nguyenvanlinh.profile.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.profile.dto.request.ProfileUpdateRequest;
import com.nguyenvanlinh.profile.dto.response.ApiResponse;
import com.nguyenvanlinh.profile.dto.response.UserProfileResponse;
import com.nguyenvanlinh.profile.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping({"/users/", "/users"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {

    UserProfileService userProfileService;

    @GetMapping("/{profileId}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String profileId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getByUserId(profileId))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserProfileResponse>> getAllProfiles() {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileService.getAllProfiles())
                .build();
    }

    @GetMapping("/my-profile")
    ApiResponse<UserProfileResponse> getMyProfile() {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getMyProfile())
                .build();
    }

    @PutMapping(
            "/{profileId}") // @Valid save value don't change -> if don't have -> update firstName = Linh, others value
    // not update -> null
    ApiResponse<UserProfileResponse> updateProfile(
            @PathVariable("profileId") String profileId, @Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.updateProfile(profileId, request))
                .build();
    }

    @DeleteMapping("/{profileId}")
    String deleteUser(@PathVariable String profileId) {
        userProfileService.deleteProfile(profileId);
        return "User " + profileId + "has been deleted!!~";
    }
}
