package com.checkout.payment.gateway.model.dto;

import com.checkout.payment.gateway.validators.AllowedCurrency;
import com.checkout.payment.gateway.validators.DateValidation;
import com.checkout.payment.gateway.validators.FieldValidation;
import com.checkout.payment.gateway.validators.FutureExpiry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@FutureExpiry(groups = DateValidation.class)
public record PostPaymentRequestDTO(

    @Schema(description = "Full card number, 14-19 numeric digits", example = "2222405343248877")
    @JsonProperty("card_number")
    @NotBlank(groups = FieldValidation.class)
    @Pattern(regexp = "\\d{14,19}", groups = FieldValidation.class)
    String cardNumber,

    @Schema(description = "Card expiry month (1-12)", example = "4")
    @JsonProperty("expiry_month")
    @Min(value= 1, groups = FieldValidation.class)
    @Max(value = 12, groups = FieldValidation.class)
    int expiryMonth,

    @Schema(description = "Card expiry year (4 digits)", example = "2030")
    @JsonProperty("expiry_year")
    int expiryYear,

    @Schema(description = "ISO 4217 currency code — GBP, USD, or EUR", example = "GBP")
    @NotBlank(groups = FieldValidation.class)
    @AllowedCurrency(groups = FieldValidation.class)
    String currency,

    @Schema(description = "Payment amount in minor units (e.g. pence for GBP)", example = "100")
    @Positive(groups = FieldValidation.class)
    int amount,

    @Schema(description = "Card CVV, 3-4 numeric digits", example = "123")
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
