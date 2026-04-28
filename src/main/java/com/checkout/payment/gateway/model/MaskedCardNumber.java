package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

// FIXME: remove
public record MaskedCardNumber(String value) {

  public MaskedCardNumber {
    Objects.requireNonNull(value, "Card number must not be null");
    if (!value.matches("\\d{14,19}")) {
      throw new IllegalArgumentException("Card number must be between 14-19 numeric characters long");
    }
    int maskLength = value.length() - 4;
    value = "*".repeat(maskLength) + value.substring(maskLength);
  }

  public String getLastFourDigits() {
    return value.substring(value.length() - 4);
  }
  @JsonProperty("expiry_month")
  @JsonValue
  public String value() {
    return value;
  }
}
