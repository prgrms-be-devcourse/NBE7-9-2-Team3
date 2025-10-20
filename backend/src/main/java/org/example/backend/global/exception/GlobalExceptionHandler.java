package org.example.backend.global.exception;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.example.backend.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 예외 처리 (ServiceException)
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse> handleServiceException(ServiceException e) {
        return new ResponseEntity<>(e.getRsData(), e.getHttpStatus());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleException(NoSuchElementException e){
        return new ApiResponse<>("404-1", "존재하지 않는 데이터입니다.");
    }

    // 요청 유효성 검사 실패 (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
            .getAllErrors()
            .stream()
            .filter(error -> error instanceof FieldError)
            .map(error -> (FieldError) error)
            .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
            .sorted(Comparator.comparing(String::toString))
            .collect(Collectors.joining(", "));

        return new ApiResponse<>("400-1", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleException(HttpMessageNotReadableException e) {
        return new ApiResponse<>("400-2", "잘못된 형식의 요청 데이터입니다.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleAllExceptions(Exception e) {
        return new ApiResponse<>("500", "서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleSecurityException(SecurityException e) {
        return new ApiResponse<>("403-1", e.getMessage() != null ? e.getMessage() : "접근 권한이 없습니다.");
    }
}