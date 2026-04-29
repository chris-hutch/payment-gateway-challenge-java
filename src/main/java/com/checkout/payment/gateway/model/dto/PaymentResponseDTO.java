package com.checkout.payment.gateway.model.dto;

import com.checkout.payment.gateway.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record PaymentResponseDTO(

    @Schema(description = "Gateway-assigned payment ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Payment outcome", example = "Authorized")
    PaymentStatus status,

    @Schema(description = "Last four digits of the card number", example = "8877")
    String cardNumberLastFour,

    @Schema(description = "Card expiry month", example = "4")
    int expiryMonth,

    @Schema(description = "Card expiry year", example = "2030")
    int expiryYear,

    @Schema(description = "ISO 4217 currency code", example = "GBP")
    String currency,

    @Schema(description = "Payment amount in minor units", example = "100")
    int amount
) {

  public PaymentResponseDTO {
    if (cardNumberLastFour.isBlank() || cardNumberLastFour.length() != 4) {
      throw new IllegalArgumentException("Invalid card number format");
    }
  }

  @Override
  public String toString() {
    return "PaymentResponseDTO{" +
        "id=" + id +
        ", status=" + status +
        ", cardNumberLastFour='" + cardNumberLastFour + '\'' +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
