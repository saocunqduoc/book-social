package com.nguyenvanlinh.notification.controller;

import com.nguyenvanlinh.event.dto.NotificationEvent;
import com.nguyenvanlinh.notification.dto.request.Recipient;
import com.nguyenvanlinh.notification.dto.request.SendEmailRequest;
import com.nguyenvanlinh.notification.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    EmailService emailService;

    @KafkaListener(topics = "notification-delivery")
    public void listenNotificationDelivery(NotificationEvent message) {
        log.info("Message received: {}", message);
        emailService.sendEmail(SendEmailRequest.builder()
                .to(Collections.singletonList(Recipient.builder()
                        .email(message.getRecipient())
                        .build()))
                .subject(message.getSubject())
                .htmlContent(message.getBody())
                .build());
    }
}
