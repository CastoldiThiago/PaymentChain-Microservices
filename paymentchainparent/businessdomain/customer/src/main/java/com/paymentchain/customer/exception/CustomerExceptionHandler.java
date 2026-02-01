package com.paymentchain.customer.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomerExceptionHandler {

    @ExceptionHandler(com.paymentchain.customer.exception.DuplicateResourceException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "duplicate_key");
        body.put("message", ex.getMessage());
        body.put("field", ex.getField());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(Exception ex) {
        String detail = ex.getMessage();
        Throwable cause = ex instanceof DataIntegrityViolationException ? ((DataIntegrityViolationException) ex).getMostSpecificCause() : ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            detail = cause.getMessage();
        }

        String field = null;
        String lower = detail != null ? detail.toLowerCase() : "";
        if (lower.contains("(email)") || lower.contains("key (email)") || lower.contains("email)")) {
            field = "email";
        } else if (lower.contains("(dni)") || lower.contains("key (dni)") || lower.contains("dni)")) {
            field = "dni";
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", "duplicate_key");
        body.put("message", field != null ? String.format("The value for '%s' already exists", field) : "Unique constraint violation");
        body.put("detail", detail);
        if (field != null) body.put("field", field);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
