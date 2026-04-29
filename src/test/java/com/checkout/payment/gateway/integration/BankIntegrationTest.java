package com.checkout.payment.gateway.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.YearMonth;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BankIntegrationTest {

  private static WireMockServer wireMockServer;

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void startWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(9999));
    wireMockServer.start();
  }

  @AfterEach
  void stopWireMock() {
    wireMockServer.stop();
  }

  @Test
  void whenCardEndingInOdd_thenPaymentIsAuthorized() throws Exception {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("{\"authorized\": true, \"authorization_code\": \"abc-123\"}")));

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(cardEndingIn("2222405343248877"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("8877"));
  }

  @Test
  void whenCardEndingInEven_thenPaymentIsDeclined() throws Exception {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("{\"authorized\": false, \"authorization_code\": \"\"}")));

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(cardEndingIn("2222405343248112"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("Declined"));
  }

  @Test
  void whenBankReturns503_thenGatewayReturns502() throws Exception {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(503)));

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(cardEndingIn("2222405343248110"))))
        .andExpect(status().isBadGateway());
  }

  @Test
  void whenPaymentCreated_thenCanBeRetrievedById() throws Exception {
    wireMockServer.stubFor(post(urlEqualTo("/payments"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("{\"authorized\": true, \"authorization_code\": \"abc-123\"}")));

    String responseBody = mvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cardEndingIn("2222405343248877"))))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    String id = objectMapper.readTree(responseBody).get("id").asText();

    mvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.status").value("Authorized"));
  }

  private PostPaymentRequestDTO cardEndingIn(String cardNumber) {
    YearMonth nextYear = YearMonth.now().plusYears(1);
    return new PostPaymentRequestDTO(
        cardNumber,
        nextYear.getMonthValue(),
        nextYear.getYear(),
        "GBP",
        100,
        "123"
    );
  }
}

