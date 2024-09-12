package com.nguyenvanlinh.identityservice.mapper;

import org.mapstruct.Mapper;

import com.nguyenvanlinh.identityservice.dto.request.ProfileCreationRequest;
import com.nguyenvanlinh.identityservice.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
