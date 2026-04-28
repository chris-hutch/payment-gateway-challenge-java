package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.NonNull;
import java.util.Objects;

public record MaskedCVV(String cvv) {

  public MaskedCVV {
    Objects.requireNonNull(cvv, "CVV must not be null");
    if (!cvv.matches("\\d{3,4}")) {
      throw new IllegalArgumentException("CVV must be 3-4 numeric characters");
    }

    cvv = "*".repeat(cvv.length());
  }

  @JsonValue
  public String cvv() {
    return cvv;
  }

  @NonNull
  @Override
  public String toString() {
    return "*".repeat(cvv.length());
  }

}
