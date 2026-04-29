package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.checkout.payment.gateway.validators.ValidationSequence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "Process and retrieve card payments")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @Operation(
      summary = "Retrieve a payment",
      description = "Returns a previously processed payment by its gateway-assigned ID."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Payment found"),
      @ApiResponse(responseCode = "404", description = "No payment with that ID")
  })

  @GetMapping(value = "/payments/{id}", produces =  MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PaymentResponseDTO> getPaymentEventById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @Operation(
      summary = "Process a payment",
      description = "Validates the request, forwards it to the acquiring bank, and returns the outcome. " +
          "Returns 201 for both Authorized and Declined outcomes. " +
          "Returns 400 with status Rejected if the request fails gateway validation."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Payment processed (Authorized or Declined)"),
      @ApiResponse(responseCode = "400", description = "Payment rejected — validation failure"),
      @ApiResponse(responseCode = "502", description = "Bank unavailable")
  })

  @PostMapping(value = "/payments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PaymentResponseDTO> processPayment(
      @Validated(ValidationSequence.class) @RequestBody PostPaymentRequestDTO paymentRequestDTO) {
    PaymentResponseDTO response = paymentGatewayService.processPayment(paymentRequestDTO);

    return ResponseEntity
        .created(URI.create("/api/v1/payments/" + response.id()))
        .body(response);
  }
}
