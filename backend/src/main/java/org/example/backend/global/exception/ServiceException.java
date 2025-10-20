package org.example.backend.global.exception;

import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {

    private final String resultCode;
    private final String msg;
    private final HttpStatus httpStatus;

    public ServiceException(String resultCode, String msg, HttpStatus httpStatus) {
        super("%s : %s".formatted(resultCode, msg));
        this.resultCode = resultCode;
        this.msg = msg;
        this.httpStatus = httpStatus;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getMsg() {
        return msg;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ApiResponse getRsData() {
        return new ApiResponse(resultCode, msg);
    }
}