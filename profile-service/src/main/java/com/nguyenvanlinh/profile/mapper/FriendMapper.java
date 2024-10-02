package com.nguyenvanlinh.profile.mapper;

import org.mapstruct.Mapper;

import com.nguyenvanlinh.profile.dto.response.FriendResponse;
import com.nguyenvanlinh.profile.entity.Friend;

@Mapper(componentModel = "spring")
public interface FriendMapper {
    FriendResponse toFriendResponse(Friend friend);
}
