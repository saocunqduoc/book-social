package com.nguyenvanlinh.profile.entity;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;
import lombok.experimental.FieldDefaults;

// khai báo entity trong neo4j => use annotation: @Node()
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    String id;

    @Property("userId")
    String userId;

    String username;
    String email;

    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    // Mối quan hệ bạn bè
    @JsonIgnore
    @Relationship(type = "FRIENDS_WITH")
    Set<Friend> friends;
}
