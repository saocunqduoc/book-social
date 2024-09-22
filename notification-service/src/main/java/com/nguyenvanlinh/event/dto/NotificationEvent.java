package com.nguyenvanlinh.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEvent {
    String channel;
    String recipient;
    String templateCode;
    Map<String, Object> param;
    // temp
    String subject;
    String body;
}
