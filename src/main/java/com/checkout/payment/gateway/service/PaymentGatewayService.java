package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.dto.BankAuthRequestDTO;
import com.checkout.payment.gateway.model.dto.BankAuthResponseDTO;
import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final BankClient bankClient;
  private final PaymentsRepository paymentsRepository;

  public PaymentGatewayService(BankClient bankClient, PaymentsRepository paymentsRepository) {
    this.bankClient = bankClient;
    this.paymentsRepository = paymentsRepository;
  }

  public PaymentResponseDTO getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id));
  }

  public PaymentResponseDTO processPayment(PostPaymentRequestDTO paymentRequest) {
    LOG.debug("Processing payment request ...");

    BankAuthRequestDTO bankAuthRequest = new BankAuthRequestDTO(
        paymentRequest.cardNumber(),
        paymentRequest.getExpiryDate(),
        paymentRequest.currency(),
        paymentRequest.amount(),
        paymentRequest.cvv()
    );

    BankAuthResponseDTO response = bankClient.authorize(bankAuthRequest);
    PaymentResponseDTO paymentResponse = buildPaymentResponse(paymentRequest, response);

    paymentsRepository.add(paymentResponse);

    return paymentResponse;
  }

  @NonNull
  private PaymentResponseDTO buildPaymentResponse(PostPaymentRequestDTO paymentRequest,
      BankAuthResponseDTO response) {
    PaymentStatus status = response.authorized()
        ? PaymentStatus.AUTHORIZED
        : PaymentStatus.DECLINED;

    return new PaymentResponseDTO(
        UUID.fromString(response.authorizationCode()),
        status,
        paymentRequest.getLastFourCardNumberDigits(),
        paymentRequest.expiryMonth(),
        paymentRequest.expiryYear(),
        paymentRequest.currency(),
        paymentRequest.amount()
    );
  }
}
