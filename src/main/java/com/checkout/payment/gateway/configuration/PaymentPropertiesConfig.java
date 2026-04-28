package com.checkout.payment.gateway.configuration;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentPropertiesConfig {

  private Set<String> allowedCurrencies;

  public Set<String> getAllowedCurrencies() {
    return allowedCurrencies;
  }

  public void setAllowedCurrencies(Set<String> allowedCurrencies) {
    this.allowedCurrencies = allowedCurrencies;
  }
}
