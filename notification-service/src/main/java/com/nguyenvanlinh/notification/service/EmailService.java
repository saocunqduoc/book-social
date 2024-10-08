package com.nguyenvanlinh.notification.service;

import com.nguyenvanlinh.notification.dto.request.EmailRequest;
import com.nguyenvanlinh.notification.dto.request.SendEmailRequest;
import com.nguyenvanlinh.notification.dto.request.Sender;
import com.nguyenvanlinh.notification.dto.response.EmailResponse;
import com.nguyenvanlinh.notification.exception.AppException;
import com.nguyenvanlinh.notification.exception.ErrorCode;
import com.nguyenvanlinh.notification.repository.httpclient.EmailClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    EmailClient emailClient;

    @Value("${notification.email.brevo-apikey}")
    @NonFinal
    String apiKey;

    public EmailResponse sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest
                .builder()
                .sender(Sender.builder()
                        .name("Book Social")
                        .email("dev.nguyenvanlinh@gmail.com")
                        .build())
                .to(request.getTo())
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch ( Exception e) {
            log.error("Error sending email", e);
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }

    }

}
