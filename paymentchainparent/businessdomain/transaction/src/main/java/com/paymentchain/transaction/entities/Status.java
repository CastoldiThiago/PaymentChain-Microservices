/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.transaction.entities;

/**
 *
 * @author casto
 */
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
    PENDIENTE("01"),
    LIQUIDADA("02"),
    RECHAZADA("03"),
    CANCELADA("04");

    private final String code;

    Status(String code) {
        this.code = code;
    }

    @JsonValue
    public String getName() {
        return this.name();
    }

    public String getCode() {
        return this.code;
    }

    public static Status fromCode(String code) {
        for (Status status : Status.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código de estado inválido: " + code);
    }

    @JsonCreator
    public static Status fromString(String value) {
        return fromCode(value);
    }
}