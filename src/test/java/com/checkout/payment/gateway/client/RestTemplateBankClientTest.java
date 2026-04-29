package com.checkout.payment.gateway.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.checkout.payment.gateway.configuration.BankPropertiesConfig;
import com.checkout.payment.gateway.exception.BankUnavailableException;
import com.checkout.payment.gateway.model.dto.BankAuthRequestDTO;
import com.checkout.payment.gateway.model.dto.BankAuthResponseDTO;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
class RestTemplateBankClientTest {

  private WireMockServer wireMockServer;

  private RestTemplateBankClient bankClient;

  @Autowired
  private RestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    wireMockServer.start();

    BankPropertiesConfig bankPropertiesConfig = new BankPropertiesConfig();
    bankPropertiesConfig.setBaseUrl(wireMockServer.baseUrl());
    bankClient = new RestTemplateBankClient(restTemplate, bankPropertiesConfig);
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void whenBankAuthorizesPayment_thenReturnsAuthorizedResponse() {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("""
                    {
                        "authorized": true,
                        "authorization_code": "0bb07405-6d44-4b50-a14f-7ae0beff13ad"
                    }
                """)));

    BankAuthResponseDTO response = bankClient.authorize(validRequest());

    assertThat(response.authorized()).isTrue();
    assertThat(response.authorizationCode()).isNotBlank();
  }

  @Test
  void whenBankDeclinesPayment_thenReturnsDeclinedResponse() {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("""
                    {
                        "authorized": false,
                        "authorization_code": ""
                    }
                """)));

    BankAuthResponseDTO response = bankClient.authorize(validRequest());

    assertThat(response.authorized()).isFalse();
  }

  @Test
  void whenBankReturns503_thenThrowsBankUnavailableException() {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(503)));

    assertThatThrownBy(() -> bankClient.authorize(validRequest()))
        .isInstanceOf(BankUnavailableException.class);
  }

  @Test
  void whenBankReturns400_thenThrowsBankUnavailableException() {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("""
                    { "error_message": "Not all required properties were sent" }
                """)));

    assertThatThrownBy(() -> bankClient.authorize(validRequest()))
        .isInstanceOf(BankUnavailableException.class);
  }

  @Test
  void whenBankIsUnreachable_thenThrowsBankUnavailableException() {
    wireMockServer.stop();

    assertThatThrownBy(() -> bankClient.authorize(validRequest()))
        .isInstanceOf(BankUnavailableException.class);
  }

  @Test
  void whenRequestSent_thenCorrectFieldsAreSentToBank() {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("""
                    { "authorized": true, "authorization_code": "abc123" }
                """)));

    bankClient.authorize(new BankAuthRequestDTO(
        "2222405343248877",
        "04/2026",
        "GBP",
        100,
        "123"
    ));

    wireMockServer.verify(postRequestedFor(urlEqualTo("/payments"))
        .withRequestBody(matchingJsonPath("$.card_number", equalTo("2222405343248877")))
        .withRequestBody(matchingJsonPath("$.expiry_date", equalTo("04/2026")))
        .withRequestBody(matchingJsonPath("$.currency", equalTo("GBP")))
        .withRequestBody(matchingJsonPath("$.amount", equalTo("100")))
        .withRequestBody(matchingJsonPath("$.cvv", equalTo("123"))));
  }

  private BankAuthRequestDTO validRequest() {
    return new BankAuthRequestDTO(
        "2222405343248877",
        "04/2026",
        "GBP",
        100,
        "123"
    );
  }
}