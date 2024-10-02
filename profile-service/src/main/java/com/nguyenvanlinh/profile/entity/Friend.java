package com.nguyenvanlinh.profile.entity;

import java.time.Instant;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("friend")
public class Friend {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    String id;

    @Property("userId")
    String userId; // ID của người dùng

    @Property("friendId")
    String friendId; // ID của bạn bè

    Instant requestSentAt; // Thời gian gửi yêu cầu
    Instant requestAcceptedAt; // Thời gian chấp nhận yêu cầu
    boolean isFriend; // Trạng thái mối quan hệ bạn bè

    public Friend(String userId, String friendId, Instant requestSentAt, Instant requestAcceptedAt, boolean isFriend) {
        this.userId = userId;
        this.friendId = friendId;
        this.requestSentAt = requestSentAt;
        this.requestAcceptedAt = requestAcceptedAt;
        this.isFriend = isFriend;
    }
}
