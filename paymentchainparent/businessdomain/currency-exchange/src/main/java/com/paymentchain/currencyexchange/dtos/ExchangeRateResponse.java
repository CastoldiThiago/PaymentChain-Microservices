package com.paymentchain.currencyexchange.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Exchange rate response")
public class ExchangeRateResponse {
    @Schema(description = "Base currency code", example = "USD")
    private String from;

    @Schema(description = "Target currency code", example = "ARS")
    private String to;

    @Schema(description = "Exchange rate value", example = "820.5")
    private BigDecimal rate;
}
