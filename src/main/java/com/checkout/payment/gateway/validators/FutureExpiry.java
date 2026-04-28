package com.checkout.payment.gateway.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;

@Constraint(validatedBy = FutureExpiryValidator.class)
@Target({ElementType.PARAMETER, TYPE, CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FutureExpiry {

  String message() default "Card expiration date must be in the future";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
