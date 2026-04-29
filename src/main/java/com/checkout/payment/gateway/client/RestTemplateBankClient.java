package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.model.dto.BankAuthRequestDTO;
import com.checkout.payment.gateway.model.dto.BankAuthResponseDTO;
import com.checkout.payment.gateway.util.CardMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateBankClient implements BankClient{

  private static final Logger LOG = LoggerFactory.getLogger(RestTemplateBankClient.class);

  private final RestTemplate restTemplate;
  private final String bankBaseUrl;

  public RestTemplateBankClient(RestTemplate restTemplate,
                                @Value("${bank.base-url}") String bankBaseUrl) {
    this.restTemplate = restTemplate;
    this.bankBaseUrl = bankBaseUrl;
  }

  @Override
  public BankAuthResponseDTO authorize(BankAuthRequestDTO request) {
    LOG.debug(
        "Authorizing bank auth request for card={}",
        CardMasker.maskCardNumber(request.cardNumber())
    );

    try {
      ResponseEntity<BankAuthResponseDTO> response = restTemplate.postForEntity(
          bankBaseUrl + "/payments",
          request,
          BankAuthResponseDTO.class
      );
      return response.getBody();

    } catch (HttpServerErrorException ex) {
      LOG.error("Bank returned server error: status={}", ex.getStatusCode());
      throw new BankUnavailableException("Bank unavailable " + ex.getStatusCode(), ex);
    } catch (HttpClientErrorException ex) {
      LOG.error("Bank returned client error: status={}", ex.getStatusCode());
      throw new BankUnavailableException("Bank rejected request " + ex.getStatusCode(), ex);
    } catch (ResourceAccessException ex) {
      LOG.error("Unable to reach bank: {}", ex.getMessage());
      throw new BankUnavailableException("Bank unreachable", ex);
    }
  }
}
