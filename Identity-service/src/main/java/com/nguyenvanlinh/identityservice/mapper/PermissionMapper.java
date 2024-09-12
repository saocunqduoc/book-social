package com.nguyenvanlinh.identityservice.mapper;

import org.mapstruct.Mapper;

import com.nguyenvanlinh.identityservice.dto.request.PermissionRequest;
import com.nguyenvanlinh.identityservice.dto.response.PermissionResponse;
import com.nguyenvanlinh.identityservice.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
