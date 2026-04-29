package com.checkout.payment.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.client.BankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.dto.BankAuthResponseDTO;
import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {
  
  @Mock
  private BankClient bankClient;

  @Mock
  private PaymentsRepository paymentsRepository;

  private PaymentGatewayService service;

  @BeforeEach
  void setUp() {
    service = new PaymentGatewayService(bankClient, paymentsRepository, new SimpleMeterRegistry());
  }

  // --- processPayment ---

  @Test
  void whenBankAuthorises_thenReturnsAuthorizedResponseAndPersists() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthResponseDTO(true, "auth-code-123"));

    PaymentResponseDTO response = service.processPayment(validRequest());

    assertThat(response.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.id()).isNotNull();

    // verify PAN was masked before persisting
    ArgumentCaptor<PaymentResponseDTO> captor = ArgumentCaptor.forClass(PaymentResponseDTO.class);
    verify(paymentsRepository).add(captor.capture());
    assertThat(captor.getValue().cardNumberLastFour()).isEqualTo("1111");
    assertThat(captor.getValue().cardNumberLastFour()).doesNotContain("1111111111111111");
  }

  @Test
  void whenBankDeclines_thenReturnsDeclinedResponseAndPersists() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthResponseDTO(false, ""));

    PaymentResponseDTO response = service.processPayment(validRequest());

    assertThat(response.status()).isEqualTo(PaymentStatus.DECLINED);

    verify(paymentsRepository).add(any());
  }

  @Test
  void whenBankUnavailable_thenThrowsAndDoesNotPersist() {
    when(bankClient.authorize(any()))
        .thenThrow(new BankUnavailableException("Bank down", new RestClientException("error")));

    assertThatThrownBy(() -> service.processPayment(validRequest()))
        .isInstanceOf(BankUnavailableException.class);

    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void whenBankAuthorises_thenResponseContainsMaskedCardNotFullPan() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthResponseDTO(true, "auth-code-123"));

    PaymentResponseDTO response = service.processPayment(validRequest());

    // last four only — never the full PAN
    assertThat(response.cardNumberLastFour()).hasSize(4);
    assertThat(response.cardNumberLastFour()).doesNotContain("111111111111");
  }

  @Test
  void whenBankAuthorises_thenCorrectExpiryAndCurrencyReturned() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthResponseDTO(true, "auth-code-123"));

    PostPaymentRequestDTO request = validRequest();
    PaymentResponseDTO response = service.processPayment(request);

    assertThat(response.expiryMonth()).isEqualTo(request.expiryMonth());
    assertThat(response.expiryYear()).isEqualTo(request.expiryYear());
    assertThat(response.currency()).isEqualTo(request.currency());
    assertThat(response.amount()).isEqualTo(request.amount());
  }

  // --- getPaymentById ---

  @Test
  void whenPaymentExists_thenReturnsPayment() {
    PaymentResponseDTO payment = authorizedPayment();
    when(paymentsRepository.get(payment.id())).thenReturn(Optional.of(payment));

    PaymentResponseDTO result = service.getPaymentById(payment.id());

    assertThat(result).isEqualTo(payment);
  }

  @Test
  void whenPaymentDoesNotExist_thenThrowsPaymentNotFoundException() {
    UUID id = UUID.randomUUID();
    when(paymentsRepository.get(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getPaymentById(id))
        .isInstanceOf(PaymentNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  // --- helpers ---

  private PostPaymentRequestDTO validRequest() {
    YearMonth nextYear = YearMonth.now().plusYears(1);
    return new PostPaymentRequestDTO(
        "1111111111111111",
        nextYear.getMonthValue(),
        nextYear.getYear(),
        "GBP",
        100,
        "123"
    );
  }

  private PaymentResponseDTO authorizedPayment() {
    return new PaymentResponseDTO(
        UUID.randomUUID(),
        PaymentStatus.AUTHORIZED,
        "1111",
        12,
        2030,
        "GBP",
        100
    );
  }
}
