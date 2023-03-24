package com.broker.trading_app.dto;

import com.broker.trading_app.validator.Symbol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record TradeDTO(
        @Symbol
        String symbol,
        @Valid
        @Min(value = 1, message = "quantity must be greater than 0 and less than or equal to 1M")
        @Max(value = 1000000, message = "quantity must be greater than 0 and less than or equal to 1M")
        long quantity,
        @Valid
        @Positive(message = "price must be greater than 0")
        double price) {
}
