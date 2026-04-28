package com.checkout.payment.gateway.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.lang.NonNull;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AllowedCurrencyValidator implements ConstraintValidator<AllowedCurrency, String> {

  private Set<String> allowedCurrencies;

  @Override
  public void initialize(@NonNull AllowedCurrency allowedCurrency) {
    Class<? extends Enum<?>> value = allowedCurrency.value();
    Enum<?>[] enums = value.getEnumConstants();

    allowedCurrencies = Arrays.stream(enums).map(Enum::name).collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return allowedCurrencies.contains(value);
  }
}
