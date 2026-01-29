/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.exception;

import org.springframework.http.HttpStatus;

/**
 *
 * @author sotobotero
 */
public class BussinesRuleException extends Exception{
  
    private long id;
    private String code;   
    private HttpStatus httpStatus;
    
    public BussinesRuleException(long id, String code, String message,HttpStatus httpStatus) {
        super(message);
        this.id = id;
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BussinesRuleException(String code, String message,HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
    
    public BussinesRuleException(String message, Throwable cause) {
        super(message, cause);
    }     

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }    

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }   

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }    
    
}
