package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.metric.GatewayMetric;
import com.checkout.payment.gateway.model.dto.BankAuthRequestDTO;
import com.checkout.payment.gateway.model.dto.BankAuthResponseDTO;
import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.util.CardMasker;
import io.micrometer.core.instrument.MeterRegistry;
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
  private final MeterRegistry meterRegistry;

  public PaymentGatewayService(BankClient bankClient, PaymentsRepository paymentsRepository,
      MeterRegistry meterRegistry) {
    this.bankClient = bankClient;
    this.paymentsRepository = paymentsRepository;
    this.meterRegistry = meterRegistry;
  }

  public PaymentResponseDTO getPaymentById(UUID id) {
    LOG.info("Requesting access to payment with ID {}", id);
    return paymentsRepository.get(id)
        .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + id));
  }

  public PaymentResponseDTO processPayment(PostPaymentRequestDTO paymentRequest) {
    LOG.info(
        "Processing card payment request card={} currency={} amount={}",
        CardMasker.maskCardNumber(paymentRequest.cardNumber()),
        paymentRequest.currency(),
        paymentRequest.amount()
    );
    try {
      BankAuthRequestDTO bankAuthRequest = new BankAuthRequestDTO(
          paymentRequest.cardNumber(),
          paymentRequest.getExpiryDate(),
          paymentRequest.currency(),
          paymentRequest.amount(),
          paymentRequest.cvv()
      );

      BankAuthResponseDTO response = bankClient.authorize(bankAuthRequest);

      LOG.info(
          "Bank auth response authorized={} card={}",
          response.authorized(),
          CardMasker.maskCardNumber(paymentRequest.cardNumber())
      );

      PaymentResponseDTO paymentResponse = buildPaymentResponse(paymentRequest, response);

      recordPaymentProcessOutcome(paymentResponse.status());

      paymentsRepository.add(paymentResponse);

      return paymentResponse;

    } catch (BankUnavailableException ex) {
      recordPaymentProcessOutcomeBankError();
      throw ex;
    }
  }

  @NonNull
  private PaymentResponseDTO buildPaymentResponse(PostPaymentRequestDTO paymentRequest,
      BankAuthResponseDTO response) {
    PaymentStatus status = response.authorized()
        ? PaymentStatus.AUTHORIZED
        : PaymentStatus.DECLINED;

    return new PaymentResponseDTO(
        UUID.randomUUID(),
        status,
        paymentRequest.getLastFourCardNumberDigits(),
        paymentRequest.expiryMonth(),
        paymentRequest.expiryYear(),
        paymentRequest.currency(),
        paymentRequest.amount()
    );
  }

  private void recordPaymentProcessOutcome(PaymentStatus paymentStatus) {
    meterRegistry.counter(
        GatewayMetric.PAYMENT_PROCESSED.getMetricName(),
        "outcome",
        paymentStatus.getName().toLowerCase()
    ).increment();
  }

  private void recordPaymentProcessOutcomeBankError() {
    meterRegistry.counter(
        GatewayMetric.PAYMENT_PROCESSED.getMetricName(),
        "outcome",
       "bank_error"
    ).increment();
  }
}
