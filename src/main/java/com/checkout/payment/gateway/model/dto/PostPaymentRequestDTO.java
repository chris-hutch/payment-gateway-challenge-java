package com.checkout.payment.gateway.model.dto;

import com.checkout.payment.gateway.validators.AllowedCurrency;
import com.checkout.payment.gateway.validators.FutureExpiry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@FutureExpiry
public record PostPaymentRequestDTO(@JsonProperty("card_number")
                                    @NotBlank @Pattern(regexp = "\\d{14,19}") String cardNumber,
                                    @JsonProperty("expiry_month")
                                    @Min(1) @Max(12) int expiryMonth,
                                    @JsonProperty("expiry_year")
                                    int expiryYear,
                                    @AllowedCurrency String currency,
                                    @Positive int amount,
                                    @NotBlank @Pattern(regexp = "\\d{3,4}") String cvv) {

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%02d/%d", expiryMonth, expiryYear);
  }

  @JsonIgnore
  public String getLastFourCardNumberDigits() {
    return cardNumber.substring(cardNumber.length() - 4);
  }

  @Override
  public String toString() {
    int cardNumberMaskLength = cardNumber.length() - 4;
    return "PostPaymentRequestDTO{" +
        "cardNumber='" + "*".repeat(cardNumberMaskLength) + cardNumber.substring(cardNumberMaskLength) + '\'' +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv='" + "*".repeat(cvv.length()) + '\'' +
        '}';
  }
}
