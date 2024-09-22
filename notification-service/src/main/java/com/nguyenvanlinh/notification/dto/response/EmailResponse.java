package com.nguyenvanlinh.notification.dto.response;

import com.nguyenvanlinh.notification.dto.request.Recipient;
import com.nguyenvanlinh.notification.dto.request.Sender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailResponse {
    String messageId;
}
