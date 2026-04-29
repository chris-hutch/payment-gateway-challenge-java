package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentsRepository {

  private final Map<UUID, PaymentResponseDTO> payments = new ConcurrentHashMap<>();

  public void add(PaymentResponseDTO payment) {
    payments.put(payment.id(), payment);
  }

  public Optional<PaymentResponseDTO> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}
