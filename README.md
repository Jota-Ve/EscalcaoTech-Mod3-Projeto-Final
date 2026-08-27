# Transfer Scheduling API

A REST API for scheduling bank transfers, built with Spring Boot. It lets a client schedule a
money transfer between two accounts for a future date and time, calculates the applicable fee
based on how many calendar days in advance the transfer is scheduled, and lets scheduled
transfers be queried, listed, or cancelled.

## Technologies

| Layer                | Technology                                      |
|-----------------------|--------------------------------------------------|
| Language / Runtime    | Java 17                                          |
| Framework             | Spring Boot 3.4.13 (Spring Framework 6.2.15)     |
| Web                   | Spring Web (REST controllers)                    |
| Persistence           | Spring Data JPA / Hibernate                      |
| Database              | H2 (in-memory)                                   |
| Schema migrations     | Flyway                                           |
| Security              | Spring Security + JWT (jjwt 0.12.6)              |
| API documentation     | springdoc-openapi / Swagger UI                   |
| Object mapping        | MapStruct                                        |
| Boilerplate reduction | Lombok                                           |
| Build tool            | Maven                                            |
| Test coverage         | JaCoCo                                           |

## What the project does

The API models the domain of **scheduling bank transfers**. A user authenticates with a
username/password to receive a JWT, then uses that token to schedule transfers, list them,
look one up by id, or cancel one.

`scheduledDate` and `transferDate` are both stored as date-times (`LocalDateTime`); the fee is
always based on the **calendar day** difference between them (the time of day is ignored for
the fee calculation, but matters for the cancellation cutoff — see below).

When a transfer is scheduled, a **fee is calculated automatically** based on how many days
separate the scheduling date from the requested transfer date:

| Days in advance | Fee                                  |
|------------------|---------------------------------------|
| 0 (same day)     | 2.5% of the amount                    |
| 1 to 10 days     | Flat fee of 10.00                     |
| 11 to 20 days    | 8.2% of the amount                    |
| 21 to 30 days    | 6.9% of the amount                    |
| 31 to 40 days    | 4.7% of the amount                    |
| 41 to 50 days    | 1.7% of the amount                    |
| more than 50 days| Rejected — scheduling is not allowed  |

If the transfer date is the same day it is scheduled, the transfer is created directly with
status `COMPLETED`; otherwise it starts as `SCHEDULED` and can later be `CANCELLED`.

## Use cases

1. **Schedule a transfer** — `POST /api/transfers`
   Creates a new transfer for a source account, destination account, amount and transfer date.
   Both the source and destination accounts must already exist (checked against the `accounts`
   table); if either does not, the request is rejected with `404 Not Found`. The fee is
   calculated automatically from the rules above; requests further than 50 days out are
   rejected with `400 Bad Request`.

2. **List all transfers** — `GET /api/transfers`
   Returns every transfer that has been scheduled.

3. **Find a transfer by id** — `GET /api/transfers/{id}`
   Returns a single transfer, or `404 Not Found` if the id does not exist.

4. **Cancel a scheduled transfer** — `PATCH /api/transfers/{id}/cancel`
   Cancels a transfer, enforcing two business rules:
   - Only transfers with status `SCHEDULED` can be cancelled (a `COMPLETED` or already
     `CANCELLED` transfer is rejected with `400 Bad Request`).
   - Cancellation is only allowed **before the exact transfer date and time**; once that
     moment has passed, the transfer can no longer be cancelled (`400 Bad Request`).

All `/api/transfers/**` endpoints require a valid JWT. Authentication itself is handled by:

- **Login** — `POST /api/auth/login`
  Authenticates a user and returns a JWT bearer token (public endpoint, no token required).

## Project structure

The code is organized by technical layer under `com.ada.transferscheduling`:

```
controller/   REST endpoints (AuthController, TransferController)
service/      Business rules (TransferService, TransferFeeCalculator)
repository/   Spring Data JPA repositories
entity/       JPA entities (Transfer, TransferStatus, Account, User)
dto/          Request/response DTOs used by the controllers
mapper/       MapStruct mapper between DTOs and entities
security/     JWT provider, filter, entry point, UserDetailsService
exception/    Custom exceptions and the global exception handler
config/       Security and OpenAPI (Swagger) configuration
```

## How to run

### Prerequisites

- Java 17
- Maven 3.9+ (a wrapper is not included; use a locally installed Maven)

### A note on Maven repositories

To build with the public Maven Central repository instead, a local
[`.mvn-settings.xml`](.mvn-settings.xml) is included in the project root. Pass it explicitly
with `-s` on every Maven command, as shown below. If your own Maven `settings.xml` already
resolves Maven Central, you can omit `-s .mvn-settings.xml` from all commands.

### Run the application

```bash
mvn -s .mvn-settings.xml spring-boot:run
```

The API starts on **http://localhost:8080**. On startup, Flyway automatically creates the
schema and seeds the H2 in-memory database with test users and sample transfers — no manual
setup is required.

### Run the tests / generate the coverage report

```bash
mvn -s .mvn-settings.xml clean test
```

The JaCoCo HTML report is generated at `target/site/jacoco/index.html`.

### Explore the API

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI spec**: http://localhost:8080/v3/api-docs
- **H2 console**: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:transferschedulingdb`, user `sa`, empty password)

### Test users (seeded by Flyway)

| Username | Password  | Roles                    |
|----------|-----------|---------------------------|
| admin    | admin123  | `ROLE_ADMIN`, `ROLE_USER` |
| user     | user123   | `ROLE_USER`               |

### Seeded accounts (seeded by Flyway)

Only these account numbers exist and can be used as `sourceAccount`/`destinationAccount` when
scheduling a transfer; any other account number is rejected with `404 Not Found`.

| Account number | Owner                     |
|-----------------|----------------------------|
| 00001-1         | Alice Johnson              |
| 00002-2         | Bob Smith                  |
| 00003-3         | Carol Davis                |
| 00004-4         | David Lee                  |
| 11111-1         | Test Source Account        |
| 22222-2         | Test Destination Account   |

### Example: login and schedule a transfer

```bash
# 1. Log in and grab the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

# 2. Schedule a transfer
curl -X POST http://localhost:8080/api/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sourceAccount":"11111-1","destinationAccount":"22222-2","amount":1000.00,"transferDate":"2026-09-01T10:00:00"}'

# 3. Cancel it
curl -X PATCH http://localhost:8080/api/transfers/{id}/cancel \
  -H "Authorization: Bearer $TOKEN"
```
### Git
```bash
https://github.com/Jota-Ve/EscalcaoTech-Mod3-Projeto-Final.git
```

### Membros do projeto
- João Vitor Barbosa
- Leonardo da Conceição
- Raphael de Freitas
- Thaís França