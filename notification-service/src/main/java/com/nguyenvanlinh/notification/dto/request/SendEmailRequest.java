package com.nguyenvanlinh.notification.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendEmailRequest {
    List<Recipient> to;
    String subject;
    String htmlContent;
}
