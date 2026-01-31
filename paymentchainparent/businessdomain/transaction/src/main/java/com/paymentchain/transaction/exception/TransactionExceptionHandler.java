package com.paymentchain.transaction.exception;

import com.paymentchain.transaction.common.StandarizedApiExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
public class TransactionExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleBusinessRule(BusinessRuleException ex) {
        StandarizedApiExceptionResponse body = new StandarizedApiExceptionResponse(
                "/errors/business-rule",
                "Business rule violation",
                ex.getCode() != null ? ex.getCode() : "BUS-001",
                ex.getMessage()
        );
        body.setInstance("/transactions");
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                                         WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        StandarizedApiExceptionResponse body = new StandarizedApiExceptionResponse(
                "/errors/validation",
                "Validation error",
                "VAL-001",
                details
        );
        body.setInstance(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                                         WebRequest request) {
        String detail = ex.getMessage();
        StandarizedApiExceptionResponse body = new StandarizedApiExceptionResponse(
                "/errors/malformed-json",
                "Malformed JSON request",
                "JSON-001",
                detail
        );
        body.setInstance(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandarizedApiExceptionResponse> handleGeneric(Exception ex, WebRequest request) {
        StandarizedApiExceptionResponse body = new StandarizedApiExceptionResponse(
                "/errors/internal",
                "Internal server error",
                "INT-001",
                ex.getMessage()
        );
        body.setInstance(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
