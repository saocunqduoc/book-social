package com.nguyenvanlinh.profile.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.profile.dto.request.ProfileCreationRequest;
import com.nguyenvanlinh.profile.dto.request.ProfileUpdateRequest;
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
    UserProfileResponse getProfile(@PathVariable String profileId) {
        return userProfileService.getProfile(profileId);
    }

    @GetMapping
    List<UserProfileResponse> getAllProfiles() {
        return userProfileService.getAllProfiles();
    }

    @PutMapping(
            "/{profileId}") // @Valid save value don't change -> if don't have -> update firstName = Linh, others value
    // not update -> null
    UserProfileResponse updateProfile(
            @PathVariable String profileId, @Valid @RequestBody ProfileUpdateRequest request) {
        return userProfileService.updateProfile(profileId, request);
    }

    @DeleteMapping("/{profileId}")
    String deleteUser(@PathVariable String profileId) {
        userProfileService.deleteProfile(profileId);
        return "User " + profileId + "has been deleted!!~";
    }
}
