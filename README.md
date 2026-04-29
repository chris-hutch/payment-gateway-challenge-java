# Checkout Payment Gateway Take Home Task
A Spring Boot payment gateway that accepts card payments requests, validates them, forwards them to 
an acquiring bank and returns the result

---

## Running
**Requirements:** JDK 17, Docker

**Local (gateway only):**
```bash
./gradlew bootRun
```
Gateway starts on `http://localhost:8090`. Swagger UI at `http://localhost:8090/swagger-ui/index.html`.

Requires the bank simulator running separately:
```bash
docker-compose up bank_simulator
```

**Full stack (gateway + bank simulator):**
```bash
docker-compose up --build
```

Both services start on a shared network. The gateway waits for the bank simulator to pass its healthcheck before starting.

## Testing

**Unit + integration tests:**
```bash
./gradlew test
```

**Testcontainers note (Colima users):**
```bash
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
./gradlew test
```

Docker Desktop users need no extra configuration.

**Smoke test against running stack:**
```bash
# Authorized (card ending odd)
curl -s -X POST http://localhost:8090/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"card_number":"2222405343248877","expiry_month":4,"expiry_year":2030,"currency":"GBP","amount":100,"cvv":"123"}' | jq .
 
# Declined (card ending even)
curl -s -X POST http://localhost:8090/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"card_number":"2222405343248112","expiry_month":4,"expiry_year":2030,"currency":"GBP","amount":100,"cvv":"123"}' | jq .
 
# 502 (card ending 0, bank unavailable)
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8090/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"card_number":"2222405343248110","expiry_month":4,"expiry_year":2030,"currency":"GBP","amount":100,"cvv":"123"}'
 
# 400 Rejected (invalid card number)
curl -s -X POST http://localhost:8090/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"card_number":"1234","expiry_month":4,"expiry_year":2030,"currency":"GBP","amount":100,"cvv":"123"}' | jq .
```

## API

### POST /api/v1/payments

Process a payment.

**Request:**
```json
{
  "card_number": "2222405343248877",
  "expiry_month": 4,
  "expiry_year": 2030,
  "currency": "GBP",
  "amount": 100,
  "cvv": "123"
}
```

| Field        | Type   | Rules                                     |
|--------------|--------|-------------------------------------------|
| card_number  | String | 14-19 numeric digits                      |
| expiry_month | int    | 1-12                                      |
| expiry_year  | int    | Combined with month must be in the future |
| currency     | String | GBP, USD, or EUR                          |
| amount       | int    | Positive integer, minor units             |
| cvv          | String | 3-4 numeric digits                        |

**Responses:**

`201 Created` — bank was called, payment authorized or declined:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "Authorized",
  "card_number_last_four": "8877",
  "expiry_month": 4,
  "expiry_year": 2030,
  "currency": "GBP",
  "amount": 100
}
```

`400 Bad Request` — gateway rejected the request before calling the bank:
```json
{
  "status": "Rejected",
  "errors": ["cardNumber: must match \\d{14,19}"]
}
```

`502 Bad Gateway` — bank was unreachable or returned a 5xx.

### GET /api/v1/payments/{id}

Retrieve a previously processed payment by its gateway-assigned ID.

`200 OK` — same shape as the POST 201 response.
`404 Not Found` — no payment with that ID in the current session.

## Design decisions

**Layering**

Controller handles HTTP concerns only: deserialisation, `@Valid`, response codes. Service orchestrates the bank call and builds the response. `BankClient` is an interface with a single `RestTemplate` implementation. This allows for easier testability without HTTP. 

**Validation**

Bean Validation runs at the controller boundary via `@Valid`. Custom constraints: `@FutureExpiry` (class-level, checks month + year combined using `YearMonth`) and `@AllowedCurrency` (field-level, backed by a configurable `Set<String>`). `@GroupSequence` ensures field-level constraints run before the cross-field expiry check which prevents a confusing "expiry in the past" error when the month itself is invalid.

**Card Number masking**

`MaskedCardNumber` is a Java record with a constructor that validates the raw card number, masks all but the last four digits, and stores the masked form. The full card number passes to the bank in `BankAuthRequestDTO` but never to `PaymentResponseDTO`, the repository, or any log line.

**Rejected vs Declined**

`Rejected` and `Declined` are distinct outcomes with different semantics. `Declined` means the bank was called and refused the card. The payment exists and is persisted. `Rejected` means the gateway refused the request before calling the bank and therefore, no payment was created. They return different HTTP status codes (201 vs 400) and different response shapes: `PaymentResponseDTO` for Authorized/Declined, `RejectedPaymentResponse` for Rejected.

**Error mapping**

`CommonExceptionHandler` maps four exception types:

| Exception                         | HTTP | Meaning                                                |
|-----------------------------------|------|--------------------------------------------------------|
| `MethodArgumentNotValidException` | 400  | Validation failure — returns `RejectedPaymentResponse` |
| `PaymentNotFoundException`        | 404  | Unknown payment ID                                     |
| `BankUnavailableException`        | 502  | Bank 5xx, connection failure, or timeout               |
| `Exception`                       | 500  | Catch-all — sanitised message, no internals exposed    |

**Currency allowlist**

Supported currencies are GBP, USD, EUR. The list is config-driven via `payment.allowed-currencies` in `application.properties`. `AllowedCurrencyValidator` is a Spring-managed bean so the config is injected via `@ConfigurationProperties`.

**Observability**

All log output is structured JSON via `logstash-logback-encoder`. A `RequestIdFilter` runs at highest precedence: it reads `X-Request-Id` from the inbound request (or generates one), puts it into MDC, and echoes it on the response header. Every log line in a request therefore carries the request ID automatically. MDC is cleared in a `finally` block to prevent leakage across thread-pool-reused requests. Micrometer counter `payments.processed` tagged with `outcome=authorized|declined|rejected|bank_error` is incremented on every payment attempt. Actuator exposes `/actuator/health` and `/actuator/metrics`.

## Assumptions

- **Currencies** are GBP, USD, EUR. The spec says "a set of at least 3". 
- **Rejected response shape.** The spec defines `Rejected` as a payment status but doesn't specify the response shape for that outcome. I return a `RejectedPaymentResponse` with `status: "Rejected"` and a field-level `errors` list rather than a `PaymentResponseDTO` with null fields, because no payment was created, returning a payment-shaped object would be misleading.
- **Storage** is in-memory per the spec. Payments do not survive a restart.
- **Currency comparison** is case-insensitive. `gbp` is rejected; `GBP` is accepted.
- **Expiry validation** treats the current month as invalid. A card expiring this month may already be past its last valid transaction date depending on the day.
- **Bank errors** (5xx, connection failure) result in a 502 with no payment persisted. The request is safe to retry.

## Out of scope

**Idempotency keys.** Production payment APIs require idempotency to prevent double-charges on retried requests. The standard approach is an `Idempotency-Key` request header stored alongside the payment. On retry with the same key, return the original response without re-calling the bank.

**Persistence.** The repository is an in-memory `ConcurrentHashMap`. For production: replace with JPA + PostgreSQL. `PaymentResponseDTO` maps to a `payments` table with UUID primary key. Sensitive fields (card last four, currency) are fine to persist; CVV is never stored.

**Retries with circuit breaker.** The bank client makes a single attempt. For production: Resilience4j with exponential backoff on `BankUnavailableException`, circuit breaker to stop hammering a degraded bank, and fallback to `BANK_ERROR` status rather than propagating a 502.

**Rate limiting.** No per-merchant rate limiting. For production: a sliding window counter in Redis keyed by merchant API key, returning `429 Too Many Requests` with `Retry-After` header.

**Secrets management.** `BANK_BASE_URL` is an env var. For production: API keys, database credentials, and any sensitive config go into AWS Secrets Manager or HashiCorp Vault, injected at runtime via the Spring Cloud Config or environment bindings.

**Audit log.** No tamper-evident record of payment attempts. For production: append-only audit events (payment attempted, authorized, declined, bank error) written to a separate store.

---

## Instructions for candidates

This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

### Requirements
- JDK 17
- Docker

### Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator


### API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

**Feel free to change the structure of the solution, use a different library etc.**