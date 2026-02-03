package com.paymentchain.currencyexchange.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateApiResponse {
    private String result; // "success"

    @JsonProperty("conversion_rate") // Mapeamos el snake_case del JSON a camelCase de Java
    private BigDecimal conversionRate;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("target_code")
    private String targetCode;
}