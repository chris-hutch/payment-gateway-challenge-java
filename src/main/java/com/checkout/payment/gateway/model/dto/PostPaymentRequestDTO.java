package com.checkout.payment.gateway.model.dto;

import com.checkout.payment.gateway.validators.AllowedCurrency;
import com.checkout.payment.gateway.validators.DateValidation;
import com.checkout.payment.gateway.validators.FieldValidation;
import com.checkout.payment.gateway.validators.FutureExpiry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@FutureExpiry(groups = DateValidation.class)
public record PostPaymentRequestDTO(

    @JsonProperty("card_number")
    @NotBlank(groups = FieldValidation.class)
    @Pattern(regexp = "\\d{14,19}", groups = FieldValidation.class)
    String cardNumber,

    @JsonProperty("expiry_month")
    @Min(value= 1, groups = FieldValidation.class)
    @Max(value = 12, groups = FieldValidation.class)
    int expiryMonth,

    @JsonProperty("expiry_year")
    int expiryYear,

    @NotBlank(groups = FieldValidation.class)
    @AllowedCurrency(groups = FieldValidation.class)
    String currency,

    @Positive(groups = FieldValidation.class)
    int amount,

    @NotBlank(groups = FieldValidation.class)
    @Pattern(regexp = "\\d{3,4}", groups = FieldValidation.class)
    String cvv
) {

  @JsonIgnore
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
