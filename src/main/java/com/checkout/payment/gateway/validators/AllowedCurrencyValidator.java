package com.checkout.payment.gateway.validators;

import com.checkout.payment.gateway.configuration.PaymentPropertiesConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AllowedCurrencyValidator implements ConstraintValidator<AllowedCurrency, String> {

  private final Set<String> allowedCurrencies;

  public AllowedCurrencyValidator(PaymentPropertiesConfig paymentPropertiesConfig) {
    this.allowedCurrencies = paymentPropertiesConfig.getAllowedCurrencies();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }
    return allowedCurrencies.contains(value.toUpperCase());
  }
}
