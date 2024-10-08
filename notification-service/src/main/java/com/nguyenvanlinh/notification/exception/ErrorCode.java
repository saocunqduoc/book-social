package com.nguyenvanlinh.notification.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED(9999, "Uncategorized error!", HttpStatus.INTERNAL_SERVER_ERROR), // exception chưa được khai báo
    INVALID_KEY(1001, "Invalid message key!", HttpStatus.BAD_REQUEST), // enum key truyền vào không đúng
    USER_EXISTED(1002, "User already existed!", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed!", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission!", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(1008, "Role not existed!", HttpStatus.NOT_FOUND),
    INVALID_DOB(1009, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    WRONG_USERNAME_PASSWORD(1010, "Username or Password is not correct!", HttpStatus.BAD_REQUEST),
    CAN_NOT_CREATE_TOKEN(1011, "Can't create token!", HttpStatus.BAD_REQUEST),
    USER_CREATION_FAILED(1012, "Can't create user!", HttpStatus.BAD_REQUEST),
    CANNOT_SEND_EMAIL(1013, "Can't send email!", HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
