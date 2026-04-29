package com.checkout.payment.gateway.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.model.dto.PostPaymentRequestDTO;
import java.time.Duration;
import java.time.YearMonth;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MountebankIntegrationTest {

  @Container
  static GenericContainer<?> mountebank = new GenericContainer<>("bbyars/mountebank:2.8.1")
      .withExposedPorts(2525, 8080)
      .withCommand("--configfile /imposters/bank_simulator.ejs --allowInjection")
      .withClasspathResourceMapping(
          "imposters/bank_simulator.ejs",   // classpath location
          "/imposters/bank_simulator.ejs",  // container location
          BindMode.READ_ONLY
      )
      .waitingFor(Wait.forHttp("/").forPort(2525).withStartupTimeout(Duration.ofSeconds(30)));

  @DynamicPropertySource
  static void overrideBankUrl(DynamicPropertyRegistry registry) {
    registry.add("bank.base-url", () ->
        "http://localhost:" + mountebank.getMappedPort(8080));
  }
  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void whenCardEndingInOdd_thenAuthorized() {
    ResponseEntity<PaymentResponseDTO> response = restTemplate.postForEntity(
        "/api/v1/payments",
        cardEndingIn("2222405343248877"),
        PaymentResponseDTO.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().status()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.getBody().cardNumberLastFour()).isEqualTo("8877");
  }

  @Test
  void whenCardEndingInEven_thenDeclined() {
    ResponseEntity<PaymentResponseDTO> response = restTemplate.postForEntity(
        "/api/v1/payments",
        cardEndingIn("2222405343248112"),
        PaymentResponseDTO.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().status()).isEqualTo(PaymentStatus.DECLINED);
  }

  @Test
  void whenCardEndingInZero_thenGatewayReturns502() {
    ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
        "/api/v1/payments",
        cardEndingIn("2222405343248110"),
        ErrorResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
  }

  @Test
  void whenPaymentAuthorized_thenCanBeRetrievedById() {
    ResponseEntity<PaymentResponseDTO> postResponse = restTemplate.postForEntity(
        "/api/v1/payments",
        cardEndingIn("2222405343248877"),
        PaymentResponseDTO.class
    );

    UUID id = postResponse.getBody().id();

    ResponseEntity<PaymentResponseDTO> getResponse = restTemplate.getForEntity(
        "/api/v1/payments/" + id,
        PaymentResponseDTO.class
    );

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().id()).isEqualTo(id);
    assertThat(getResponse.getBody().status()).isEqualTo(PaymentStatus.AUTHORIZED);
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
