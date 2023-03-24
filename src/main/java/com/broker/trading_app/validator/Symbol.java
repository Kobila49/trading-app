package com.broker.trading_app.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SymbolValidator.class)
@Documented
public @interface Symbol {

    String message() default "Symbol valid values: USD/JPY, EUR/USD";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
