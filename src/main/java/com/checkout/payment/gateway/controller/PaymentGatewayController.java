package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.checkout.payment.gateway.validators.ValidationSequence;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @GetMapping(value = "/payments/{id}", produces =  MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PaymentResponseDTO> getPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @PostMapping(value = "/payments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PaymentResponseDTO> processPayment(
      @Validated(ValidationSequence.class) @RequestBody PostPaymentRequestDTO paymentRequestDTO) {
    PaymentResponseDTO response = paymentGatewayService.processPayment(paymentRequestDTO);

    return ResponseEntity
        .created(URI.create("/api/v1/payments/" + response.id()))
        .body(response);
  }
}
