package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public record MaskedCardNumber(String cardNumber) {

  public MaskedCardNumber {
    Objects.requireNonNull(cardNumber, "Card number must not be null");
    if (!cardNumber.matches("\\d{14,19}")) {
      throw new IllegalArgumentException("Card number must be between 14-19 numeric characters long");
    }
    int maskLength = cardNumber.length() - 4;
    cardNumber = "*".repeat(maskLength) + cardNumber.substring(maskLength);
  }

  @JsonValue
  public String cardNumber() {
    return cardNumber;
  }
}
