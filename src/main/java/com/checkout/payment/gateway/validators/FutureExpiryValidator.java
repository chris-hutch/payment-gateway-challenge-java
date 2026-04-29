package com.checkout.payment.gateway.validators;

import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.YearMonth;
import org.springframework.lang.NonNull;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class FutureExpiryValidator implements ConstraintValidator<FutureExpiry, PostPaymentRequestDTO> {

  @Override
  public boolean isValid(@NonNull PostPaymentRequestDTO value, @NonNull ConstraintValidatorContext context) {
    Clock clock = context.getClockProvider().getClock();

    try {
      return YearMonth.of(value.expiryYear(), value.expiryMonth()).isAfter(YearMonth.now(clock));
    } catch (DateTimeException e) {
      return false;
    }
  }
}
