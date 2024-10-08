package com.nguyenvanlinh.identityservice.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.identityservice.dto.request.*;
import com.nguyenvanlinh.identityservice.dto.response.ApiResponse;
import com.nguyenvanlinh.identityservice.dto.response.AuthenticationResponse;
import com.nguyenvanlinh.identityservice.dto.response.IntrospectResponse;
import com.nguyenvanlinh.identityservice.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    // outbound : System bên ngoài
    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> authenticateOutbound(@RequestParam("code") String code) {
        var result = authenticationService.outboundAuthentication(code);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
    // verify email
    @PostMapping("/verify")
    public ApiResponse<IntrospectResponse> verifyEmail(@RequestParam String userId, @RequestParam String otp) {
        VerifiedEmailRequest request = new VerifiedEmailRequest(userId, otp); // Create the request object
        var result = authenticationService.verifyOtp(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logOut(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refreshToken")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshRequest(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
    //    nếu không sử dụng  mapstruct
    //    @PostMapping("/log-in")
    //    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
    //        boolean result = authenticationService.authenticate(request);
    //
    //        // Tạo đối tượng AuthenticationResponse bằng cách sử dụng setter methods
    //        AuthenticationResponse authResponse = new AuthenticationResponse();
    //        authResponse.setAuthenticated(result);
    //
    //        // Tạo đối tượng ApiResponse bằng cách sử dụng setter methods
    //        ApiResponse<AuthenticationResponse> response = new ApiResponse<>();
    //        response.setResult(authResponse);
    //
    //        return response;
    //    }
}
