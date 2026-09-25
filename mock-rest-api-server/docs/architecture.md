# mock-rest-api-server — Module Architecture

For the full system overview and how this service fits into the request flow, see [../../docs/architecture.md](../../docs/architecture.md).

## Purpose

Simulates the encrypted upstream API that gateway forwards requests to. Allows fully local end-to-end testing without a real backend.

## Package Layout

```text
src/main/java/com/example/mockserver/
├── presentation/{booking,visit,support,shared}/web/  # HTTP DTOs, mappers and errors
├── application/{booking,visit,support}/             # Use cases and command/result contracts
├── domain/booking/                                 # Booking state and capacity rules
├── domain/shared/                                  # Meaningful business failures
└── config/                                         # Clock and identifier composition
```

## Expected Request Format

The mock server receives requests from gateway in this shape:

```http
POST /api/<path>
X-Checksum: <checksum>
X-Api-Key: <key>
X-Ins-Code: <org>
Content-Type: application/json

{"data": "<encrypted body>"}
```

## Expected Response Format

```json
{"data": "<encrypted response>"}
```

Gateway will attempt to decrypt `$.data` from this response. If `data` is absent, gateway returns the raw response with a warning log.

## Manual Request Examples

See `http/mock-rest-api.http` for IntelliJ/VS Code HTTP client examples to test the mock server directly.

## Service State and Time

`CatsBookingService` and `OrgAReservationService` compose separate `BookingOperations` instances.
The shared implementation does not imply shared reservation state. `RuntimeConfig` supplies a
`Clock` and UUID supplier; tests replace both with deterministic values. Booking/visit capacity
checks and state mutations are synchronized within each service store, and repeated cancellation
or resolution preserves the original result.
