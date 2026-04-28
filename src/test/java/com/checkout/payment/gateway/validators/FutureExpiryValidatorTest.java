package com.checkout.payment.gateway.validators;

import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.YearMonth;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FutureExpiryValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void whenExpiryIsNextMonth_thenValid() {
    var violations = validator.validate(validRequest());
    assertThat(violations).isEmpty();
  }

  @Test
  void whenExpiryIsCurrentMonth_thenInvalid() {
    var violations = validator.validate(invalidMonthRequest(0));

    assertThat(violations)
        .allMatch(v -> v.getConstraintDescriptor()
            .getAnnotation()
            .annotationType()
            .equals(FutureExpiry.class)
        );
  }

  @Test
  void whenExpiryIsLastMonth_thenInvalid() {
    var violations = validator.validate(invalidMonthRequest(-1));

    assertThat(violations)
        .allMatch(v -> v.getConstraintDescriptor()
            .getAnnotation()
            .annotationType()
            .equals(FutureExpiry.class)
        );
  }

  @Test
  void whenExpiryIsLastYear_thenInvalid() {
    var violations = validator.validate(invalidYearRequest());
    assertThat(violations)
        .allMatch(v -> v.getConstraintDescriptor()
            .getAnnotation()
            .annotationType()
            .equals(FutureExpiry.class)
        );
  }

  @Test
  void whenExpiryIsFarIntoTheFuture_thenValid() {
    YearMonth yearMonth = YearMonth.now().plusYears(5);

    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1111222233334444",
        yearMonth.getMonthValue(),
        yearMonth.getYear(),
        "GBP",
        100,
        "123"
    );

    var violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  private PostPaymentRequestDTO validRequest() {

    YearMonth yearMonth = YearMonth.now().plusMonths(1);

    return new PostPaymentRequestDTO(
        "1111222233334444",
        yearMonth.getMonthValue(),
        yearMonth.getYear(),
        "GBP",
        100,
        "123"
    );
  }

  private PostPaymentRequestDTO invalidMonthRequest(int monthOffset) {

    YearMonth yearMonth = YearMonth.now().plusMonths(monthOffset);

    return new PostPaymentRequestDTO(
        "1111222233334444",
        yearMonth.getMonthValue(),
        yearMonth.getYear(),
        "GBP",
        100,
        "123"
    );
  }

  private PostPaymentRequestDTO invalidYearRequest() {

    YearMonth yearMonth = YearMonth.now().minusYears(1);

    return new PostPaymentRequestDTO(
        "1111222233334444",
        yearMonth.getMonthValue(),
        yearMonth.getYear(),
        "GBP",
        100,
        "123"
    );
  }

}