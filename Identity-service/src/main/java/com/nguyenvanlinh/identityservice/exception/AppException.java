package com.nguyenvanlinh.identityservice.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        // kế thừa exception
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
