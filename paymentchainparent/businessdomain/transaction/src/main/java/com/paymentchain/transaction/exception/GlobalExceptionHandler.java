package com.paymentchain.transaction.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyReference(PropertyReferenceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Invalid sort or filter property");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<Map<String, Object>> handleIllegalArg(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(com.paymentchain.transaction.exception.DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(com.paymentchain.transaction.exception.DuplicateResourceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "duplicate_key");
        body.put("message", ex.getMessage());
        body.put("field", ex.getField());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
