package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.List;

public class RejectedPaymentResponse {

  private final List<String> errors;
  private final PaymentStatus status;

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
