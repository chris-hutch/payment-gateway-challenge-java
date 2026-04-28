package com.checkout.payment.gateway.model.dto;

import com.checkout.payment.gateway.enums.PaymentStatus;

import java.util.UUID;

public record PaymentResponseDTO(UUID id,
                                 PaymentStatus status,
                                 String cardNumberLastFour,
                                 int expiryMonth,
                                 int expiryYear,
                                 String currency,
                                 int amount) {

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
