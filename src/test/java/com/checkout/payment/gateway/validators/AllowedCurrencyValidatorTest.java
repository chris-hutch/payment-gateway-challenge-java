package com.checkout.payment.gateway.validators;


import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.configuration.PaymentPropertiesConfig;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AllowedCurrencyValidatorTest {
  private AllowedCurrencyValidator validator;

  @BeforeEach
  void setUp() {
    PaymentPropertiesConfig properties = new PaymentPropertiesConfig();
    properties.setAllowedCurrencies(Set.of("GBP", "USD", "EUR"));
    validator = new AllowedCurrencyValidator(properties);
  }

  @ParameterizedTest
  @ValueSource(strings = {"GBP", "USD", "EUR", "gbp", "usd", "eur"})
  void whenCurrencyIsAllowed_thenValid(String currency) {
    assertThat(validator.isValid(currency, null)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"JPY", "AUD", "BTC"})
  void whenCurrencyIsNotAllowed_thenInvalid(String currency) {
    assertThat(validator.isValid(currency, null)).isFalse();
  }

  @Test
  void whenCurrencyIsNull_thenInvalid() {
    assertThat(validator.isValid(null, null)).isFalse();
  }
}