package com.broker.trading_app.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class SymbolValidator implements ConstraintValidator<Symbol, String> {

    @Override
    public boolean isValid(String symbol, ConstraintValidatorContext constraintValidatorContext) {
        var acceptedSymbols = List.of("USD/JPY", "EUR/USD");
        return acceptedSymbols.contains(symbol);
    }
}
