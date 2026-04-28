package com.checkout.payment.gateway.model.dto;

import com.checkout.payment.gateway.model.Currency;
import com.checkout.payment.gateway.model.MaskedCVV;
import com.checkout.payment.gateway.model.MaskedCardNumber;
import com.checkout.payment.gateway.validators.AllowedCurrency;
import com.checkout.payment.gateway.validators.FutureExpiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Valid
@FutureExpiry
public record PostPaymentRequestDTO(@JsonProperty("card_number")
                                    MaskedCardNumber cardNumber,
                                    @JsonProperty("expiry_month")
                                    @Min(1) @Max(12)
                                    int expiryMonth,
                                    @JsonProperty("expiry_year")
                                    int expiryYear,
                                    @AllowedCurrency(value = Currency.class)
                                    String currency,
                                    int amount,
                                    MaskedCVV cvv) {

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%02d/%d", expiryMonth, expiryYear);
  }
}
