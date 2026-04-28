package com.checkout.payment.gateway.model;

public record CardNumberLastFour(String cardNumberLastFour) {

  public CardNumberLastFour {
    validate(cardNumberLastFour);
  }

  private void validate(String cardNumberLastFour) {
    if (cardNumberLastFour == null) {
      throw new IllegalArgumentException("cardNumberLastFour cannot be null");
    }

    if  (cardNumberLastFour.length() != 4) {
      throw new IllegalArgumentException("cardNumberLastFour must be 4 characters long");
    }
  }
}
