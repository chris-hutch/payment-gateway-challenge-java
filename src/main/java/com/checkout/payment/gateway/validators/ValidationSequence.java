package com.checkout.payment.gateway.validators;

import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import jakarta.validation.GroupSequence;

@GroupSequence({FieldValidation.class, DateValidation.class, PostPaymentRequestDTO.class})
public interface ValidationSequence {

}
