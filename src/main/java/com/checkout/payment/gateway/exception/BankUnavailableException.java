package com.checkout.payment.gateway.exception;

import org.springframework.web.client.RestClientException;

public class BankUnavailableException extends RuntimeException {

  public BankUnavailableException(String message, RestClientException ex) {
    super(message, ex);
  }
}
