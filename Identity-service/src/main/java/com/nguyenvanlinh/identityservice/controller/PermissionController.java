package com.nguyenvanlinh.identityservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nguyenvanlinh.identityservice.dto.request.ApiResponse;
import com.nguyenvanlinh.identityservice.dto.request.PermissionRequest;
import com.nguyenvanlinh.identityservice.dto.response.PermissionResponse;
import com.nguyenvanlinh.identityservice.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping({"/permissions", "/permissions/"}) // Khai báo toàn class
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{permission}")
    ApiResponse<Void> delete(@RequestBody @PathVariable String permission) {
        permissionService.delete(permission);
        return ApiResponse.<Void>builder().build();
    }
}
