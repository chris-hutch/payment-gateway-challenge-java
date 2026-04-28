package com.checkout.payment.gateway.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AllowedCurrencyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AllowedCurrency {
  String message() default "Only the following currencies are accepted: GBP, USD, EUR.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
