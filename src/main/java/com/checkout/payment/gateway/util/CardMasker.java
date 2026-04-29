package com.checkout.payment.gateway.util;

public final class CardMasker {

  private CardMasker() {}

  public static String maskCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.length() < 4) {
      return "****";
    }

    String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
    return "*".repeat(cardNumber.length() - 4) + lastFourDigits;
  }
}
