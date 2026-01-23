/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 *
 * @author casto
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class BusinessRuleException extends Exception {
    
    private long id;
    private String code;
    private HttpStatus httpStatus;
    
    public BusinessRuleException(long id, String code, String message, HttpStatus httpStatus) {
        super(message);
        this.id=id;
        this.code=code;
        this.httpStatus=httpStatus;
    }
    
    public BusinessRuleException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code=code;
        this.httpStatus=httpStatus;
    }
    
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
