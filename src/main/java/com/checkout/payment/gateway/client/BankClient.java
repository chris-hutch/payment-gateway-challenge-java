package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.model.dto.BankAuthRequestDTO;
import com.checkout.payment.gateway.model.dto.BankAuthResponseDTO;

public interface BankClient {

  BankAuthResponseDTO authorize(BankAuthRequestDTO request);

}
