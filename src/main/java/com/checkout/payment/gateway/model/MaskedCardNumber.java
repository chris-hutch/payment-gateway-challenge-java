package com.checkout.payment.gateway.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.lang.NonNull;

public record MaskedCardNumber(@NotBlank
                               @Pattern(regexp = "\\d{14,19}")
                               String cardNumber) {


  @NonNull
  @Override
  public String toString() {
    final int maskLength = cardNumber.length() - 4;
    return "*".repeat(maskLength) + cardNumber.substring(maskLength);
  }

}
