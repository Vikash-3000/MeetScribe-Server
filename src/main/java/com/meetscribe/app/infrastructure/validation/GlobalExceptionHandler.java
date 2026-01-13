package com.meetscribe.app.infrastructure.validation;

import com.meetscribe.app.common.response.ApiError;
import com.meetscribe.app.common.response.ApiResponse;
import com.meetscribe.app.core.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1️⃣ Request validation errors (DTO level)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ApiResponse.error(
                new ApiError(
                        "VALIDATION_ERROR",
                        fieldErrors.toString()
                )
        );
    }

    // 2️⃣ Domain/business rule violations
    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleDomainException(
            DomainException ex
    ) {
        return ApiResponse.error(
                new ApiError(
                        "BUSINESS_RULE_VIOLATION",
                        ex.getMessage()
                )
        );
    }

    // 3️⃣ Fallback (optional but good)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleUnexpected(
            Exception ex
    ) {
        return ApiResponse.error(
                new ApiError(
                        "INTERNAL_ERROR",
                        "Something went wrong"
                )
        );
    }
}