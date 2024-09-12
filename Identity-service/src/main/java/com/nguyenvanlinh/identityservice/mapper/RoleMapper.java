package com.nguyenvanlinh.identityservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nguyenvanlinh.identityservice.dto.request.RoleRequest;
import com.nguyenvanlinh.identityservice.dto.response.RoleResponse;
import com.nguyenvanlinh.identityservice.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
