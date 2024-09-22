package com.nguyenvanlinh.identityservice.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nguyenvanlinh.identityservice.configuration.AuthenticationRequestInterceptor;
import com.nguyenvanlinh.identityservice.dto.request.ProfileCreationRequest;
import com.nguyenvanlinh.identityservice.dto.response.ApiResponse;
import com.nguyenvanlinh.identityservice.dto.response.UserProfileResponse;

@FeignClient(
        name = "profile-service",
        url = "${app.services.profile}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface ProfileClient {
    @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileResponse> createProfile(@RequestBody ProfileCreationRequest request);
}
