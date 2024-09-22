package com.nguyenvanlinh.post.mapper;

import org.mapstruct.Mapper;

import com.nguyenvanlinh.post.dto.response.PostResponse;
import com.nguyenvanlinh.post.entity.Post;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponse toPostResponse(Post post);
}
