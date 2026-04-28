package com.checkout.payment.gateway.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.lang.NonNull;

public record MaskedCVV(@NotBlank
                        @Pattern(regexp = "\\d{3,4}")
                        String cvv) {


  @NonNull
  @Override
  public String toString() {
    return "*".repeat(cvv.length());
  }

}
