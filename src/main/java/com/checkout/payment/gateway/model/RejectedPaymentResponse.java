package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class RejectedPaymentResponse {

  @Schema(description = "Always Rejected for this response type", example = "Rejected")
  private final PaymentStatus status;

  @Schema(description = "List of validation errors", example = "[\"cardNumber: must match \\\\d{14,19}\"]")
  private final List<String> errors;

  public RejectedPaymentResponse(List<String> errors) {
    this.status = PaymentStatus.REJECTED;
    this.errors = errors;
  }

  public List<String> getErrors() {
    return errors;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return "RejectedPaymentResponse{" +
        "errors=" + errors +
        ", status=" + status +
        '}';
  }
}
