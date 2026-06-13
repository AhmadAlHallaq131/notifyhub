package com.ahmad.notifyhub.exception;

import com.ahmad.notifyhub.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException emailAlreadyExistsException,
            HttpServletRequest request
    ){
        HttpStatus httpStatus = HttpStatus.CONFLICT;

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                emailAlreadyExistsException.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(httpStatus).body(apiErrorResponse);
    }
}
