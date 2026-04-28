package com.checkout.payment.gateway.validators;


import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class AllowedCurrencyValidatorTest {
  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  private PostPaymentRequestDTO withCurrency(String currency) {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);
    return new PostPaymentRequestDTO(
        "2222405343248877",
        nextMonth.getMonthValue(),
        nextMonth.getYear(),
        currency,
        100,
        "123"
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"GBP", "USD", "EUR"})
  void whenCurrencyIsAllowed_thenValid(String currency) {
    var violations = validator.validate(withCurrency(currency));

    assertThat(violations).noneMatch(v -> v.getConstraintDescriptor()
        .getAnnotation().annotationType().equals(AllowedCurrency.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"JPY", "AUD", "BTC", "XXX", "gbp"})
  void whenCurrencyIsNotAllowed_thenInvalid(String currency) {
    var violations = validator.validate(withCurrency(currency));

    assertThat(violations).allMatch(v -> v.getConstraintDescriptor()
        .getAnnotation().annotationType().equals(AllowedCurrency.class));
  }

  @Test
  void whenCurrencyIsNull_thenInvalid() {
    var violations = validator.validate(withCurrency(null));
    assertThat(violations).isNotEmpty();
  }

  @Test
  void whenCurrencyIsBlank_thenInvalid() {
    var violations = validator.validate(withCurrency(""));
    assertThat(violations).isNotEmpty();
  }
}