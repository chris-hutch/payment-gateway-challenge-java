package com.checkout.payment.gateway.validators;

import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import org.springframework.lang.NonNull;
import java.time.Clock;
import java.time.YearMonth;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class FutureExpiryValidator implements ConstraintValidator<FutureExpiry, PostPaymentRequestDTO> {

  @Override
  public boolean isValid(@NonNull PostPaymentRequestDTO value, @NonNull ConstraintValidatorContext context) {
    Clock clock = context.getClockProvider().getClock();
    YearMonth currentYearMonth = YearMonth.now(clock);

    YearMonth requestYearMonth = YearMonth.of(value.expiryYear(), value.expiryMonth());
    return requestYearMonth.isAfter(currentYearMonth);
  }
}
