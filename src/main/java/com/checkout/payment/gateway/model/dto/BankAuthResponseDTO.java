package com.checkout.payment.gateway.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BankAuthResponseDTO(boolean authorized,
                                  @JsonProperty("authorization_code")
                                  String authorizationCode) {

}
