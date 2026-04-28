package com.checkout.payment.gateway.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FutureExpiryValidatorTest {

  private final FutureExpiryValidator validator = new FutureExpiryValidator();

  @Mock
  private ConstraintValidatorContext mockContext;

  @Mock
  private ClockProvider mockClockProvider;

  @BeforeEach
  void setUp() {
    when(mockContext.getClockProvider()).thenReturn(mockClockProvider);
    when(mockClockProvider.getClock()).thenReturn(Clock.systemUTC());
  }

  @Test
  void whenExpiryIsNextMonth_thenValid() {
    var isValid = validator.isValid(validRequest(), mockContext);

    assertThat(isValid).isTrue();
  }

  @Test
  void whenExpiryIsCurrentMonth_thenInvalid() {
    var isValid = validator.isValid(invalidMonthRequest(0), mockContext);

    assertThat(isValid).isFalse();
  }

  @Test
  void whenExpiryIsLastMonth_thenInvalid() {
    var isValid = validator.isValid(invalidMonthRequest(-1), mockContext);

    assertThat(isValid).isFalse();
  }

  @Test
  void whenExpiryIsLastYear_thenInvalid() {
    var isValid = validator.isValid(invalidYearRequest(), mockContext);

    assertThat(isValid).isFalse();
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

    var isValid = validator.isValid(request, mockContext);

    assertThat(isValid).isTrue();
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