package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.NonNull;
import java.util.Objects;

// FIXME: remove
public record MaskedCVV(String value) {

  public MaskedCVV {
    Objects.requireNonNull(value, "CVV must not be null");
    if (!value.matches("\\d{3,4}")) {
      throw new IllegalArgumentException("CVV must be 3-4 numeric characters");
    }

    value = "*".repeat(value.length());
  }

  @JsonValue
  public String value() {
    return value;
  }

  @NonNull
  @Override
  public String toString() {
    return "*".repeat(value.length());
  }

}
