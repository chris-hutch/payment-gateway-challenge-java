package com.checkout.payment.gateway.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.configuration.PaymentPropertiesConfig;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClientException;

@Import(PaymentPropertiesConfig.class)
@WebMvcTest(PaymentGatewayController.class)
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PaymentGatewayService paymentGatewayService;

  // --- GET api/v1/payments/{id} tests ---

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PaymentResponseDTO payment = new PaymentResponseDTO(
        UUID.randomUUID(),
        PaymentStatus.AUTHORIZED,
        "4321",
        12,
        2024,
        "USD",
        10
    );

    when(paymentGatewayService.getPaymentById(payment.id())).thenReturn(payment);

    mvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/" + payment.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.status().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.cardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.expiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.expiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.currency()))
        .andExpect(jsonPath("$.amount").value(payment.amount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    UUID id = UUID.randomUUID();

    when(paymentGatewayService.getPaymentById(id))
        .thenThrow(new PaymentNotFoundException("Payment not found: " + id));

    mvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Payment not found: " + id));
  }

  // --- POST api/v1/payments tests ---

  @Test
  void whenValidRequest_thenReturns201WithAuthorizedPayment() throws Exception {
    PostPaymentRequestDTO request = validPostRequest();
    PaymentResponseDTO response = authorizedResponse(request);

    when(paymentGatewayService.processPayment(any())).thenReturn(response);

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(response.cardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(response.expiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(response.expiryYear()))
        .andExpect(jsonPath("$.currency").value(response.currency()))
        .andExpect(jsonPath("$.amount").value(response.amount()));
  }

  @Test
  void whenValidRequest_thenReturns201WithDeclinedPayment() throws Exception {
    PostPaymentRequestDTO request = validPostRequest();
    PaymentResponseDTO response = declinedResponse(request);

    when(paymentGatewayService.processPayment(any())).thenReturn(response);

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(PaymentStatus.DECLINED.getName()));
  }

  @Test
  void whenCardNumberIsInvalid_thenReturns400WithRejectedStatus() throws Exception {
    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1234",
        12,
        2030,
        "GBP",
        100,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  @Test
  void whenCurrencyIsNotAllowed_thenReturns400WithRejectedStatus() throws Exception {
    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1111111111111111",
        12,
        2030,
        "JPY",
        100,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  @Test
  void whenExpiryIsInThePast_thenReturns400WithRejectedStatus() throws Exception {
    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1111111111111111",
        1,
        2020,
        "GBP",
        100,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  @Test
  void whenBankIsUnavailable_thenReturns502() throws Exception {
    when(paymentGatewayService.processPayment(any()))
        .thenThrow(new BankUnavailableException("Bank unavailable", new RestClientException("Bank unavailable")));

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validPostRequest())))
        .andExpect(status().isBadGateway());
  }

  @Test
  void whenRequestBodyIsMissing_thenReturns400() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenAmountIsZero_thenReturns400WithRejectedStatus() throws Exception {
    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1111111111111111", 12, 2030, "GBP", 0, "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  @Test
  void whenAmountIsNegative_thenReturns400WithRejectedStatus() throws Exception {
    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1111111111111111", 12, 2030, "GBP", -100, "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  @Test
  void whenCvvIsInvalid_thenReturns400WithRejectedStatus() throws Exception {
    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1111111111111111", 12, 2030, "GBP", 100, "12"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  @Test
  void whenExpiryMonthIsOutOfRange_thenReturns400WithRejectedStatus() throws Exception {
    PostPaymentRequestDTO request = new PostPaymentRequestDTO(
        "1111111111111111", 13, 2030, "GBP", 100, "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  // --- helpers

  private PostPaymentRequestDTO validPostRequest() {
    return new PostPaymentRequestDTO(
        "1111111111111111",
        12,
        2030,
        "GBP",
        100,
        "123"
    );
  }

  private PaymentResponseDTO authorizedResponse(PostPaymentRequestDTO request) {
    return new PaymentResponseDTO(
        UUID.randomUUID(),
        PaymentStatus.AUTHORIZED,
        request.getLastFourCardNumberDigits(),
        request.expiryMonth(),
        request.expiryYear(),
        request.currency(),
        request.amount()
    );
  }

  private PaymentResponseDTO declinedResponse(PostPaymentRequestDTO request) {
    return new PaymentResponseDTO(
        UUID.randomUUID(),
        PaymentStatus.DECLINED,
        request.getLastFourCardNumberDigits(),
        request.expiryMonth(),
        request.expiryYear(),
        request.currency(),
        request.amount()
    );
  }
}
