# Project Architecture

## Overview

This repository contains a local API proxy test harness and report viewer:

- **gateway** — receives plain-text requests from clients, encrypts them, forwards to an upstream API, then decrypts the response before returning it.
- **mock-rest-api-server** — simulates the upstream encrypted API, enabling fully local integration testing without a real backend.
- **karate-tests** — runs scenario-based E2E tests against the gateway.
- **report-server** — stores Karate/CATS execution history and serves report browsing UI.

## Module Structure

```text
api-test-orchestrator/
├── gateway/                    # Encrypted API proxy (port 28080)
│   └── src/main/java/com/example/gateway/
│       ├── presentation/proxy/web/
│       ├── application/proxy/{service,port,exception,result}/
│       ├── domain/routing/model/
│       ├── infrastructure/{http,crypto}/
│       └── config/
├── mock-rest-api-server/        # In-memory upstream simulation (port 18080)
│   └── src/main/java/com/example/mockserver/
│       ├── presentation/{booking,visit,support,shared}/web/
│       ├── application/{booking,visit,support}/{service,contract}/
│       ├── domain/{booking,shared}/
│       └── config/
├── karate-tests/                # Black-box HTTP scenarios; no application layers
│   └── src/test/{java/karate,resources/scenarios}/
└── report-server/               # Report UI, JSON APIs and publishing CLI (port 48080)
    ├── src/main/java/com/example/reportserver/
    │   ├── presentation/run/{web,cli}/
    │   ├── application/{run,catalog}/
    │   ├── domain/{run,catalog}/model/
    │   ├── infrastructure/{persistence,parser,catalog}/
    │   └── config/
    ├── src/main/resources/{templates,static}/
    └── data/runs/               # Runtime reports (gitignored)
```

## Request Flow

```
Client
  │  plain-text body
  ▼
Gateway
  ├─ route lookup: (org, service, api, method) → GatewayProperties
  ├─ encrypt body       → CryptoModule.encrypt(key, body)
  ├─ compute checksum   → ChecksumModule.checksum(encrypted)
  ├─ set headers        → X-Checksum, X-Api-Key, X-Ins-Code, …
  └─ POST {"data": <encrypted>} to upstream
          │
          ▼
  Mock REST API Server  (or real upstream in production)
          │  {"data": <encrypted response>}
          ▼
Gateway
  ├─ extract $.data from response
  ├─ decrypt             → CryptoModule.decrypt(key, data)
  └─ return plain-text response to client
```

If decryption fails or `$.data` is absent, gateway logs a warning and returns the raw upstream response (facilitates debugging without masking upstream errors).

## Route Resolution

Routes are configured under `gateway.apis` in `application.yml`:

```yaml
gateway:
  apis:
    - org: acme
      service: payments
      api: charge
      method: POST
      host: http://localhost:18080
      externalPath: /api/payments/charge
      key: <encryption-key>
```

Lookup key: `(org, service, api, method)`. No match → `RouteNotFoundException` → HTTP 404.

## Crypto & Checksum Interfaces

```java
interface CryptoModule {
    String encrypt(String key, String plaintext);
    String decrypt(String key, String ciphertext);
}

interface ChecksumModule {
    String checksum(String data);
}
```

Local default implementations use Base64 encoding and SHA-256. Production implementations are injected via the `libs/` JAR or Spring profiles.

## Test And Report Flow

```
Karate / CATS
  │  call gateway through /cats/{org}/{service}/{api}
  ▼
Gateway
  │  encrypted upstream call
  ▼
Mock REST API Server
  │
  ▼
Karate / CATS raw reports
  │  publish scripts
  ▼
report-server/data/runs/{runId}/
  ├─ meta.json
  ├─ cases.json
  └─ report/
      └─ original HTML/JSON assets
```

The report server exposes:

- `/` — service summary
- `/services/{org}/{service}` — run history with filters
- `/runs/{runId}` — run detail and test case table
- `/api/services` and `/api/runs` — JSON APIs for automation

## Gradle Module Boundaries

The four application/test modules have no Gradle project dependencies. They communicate through
HTTP and published report files. `build-logic` is an included plugin build, not an application module.
The root `verifyModuleGraph` task compares this block with Gradle's evaluated project dependencies.

<!-- gradle-module-graph:start -->
```text
:gateway -> []
:karate-tests -> []
:mock-rest-api-server -> []
:report-server -> []
```
<!-- gradle-module-graph:end -->

## Package Responsibilities

The selected profile is [layered-clean](../.dev-standards/standards/architectures/layered-clean.md),
configured explicitly in `.dev-standards/config.yml`. All three Spring applications use layers first,
then feature packages. Karate remains a test client, not an application with artificial layers.

Allowed compiled dependencies:

```text
presentation -> application -> domain
infrastructure -> application ports/results and domain
config / application bootstrap -> composition targets
```

- `presentation` owns routes, CLI option parsing, request/response DTOs, view models and HTTP errors.
  It invokes application use cases, never output ports or concrete infrastructure. Domain value types
  may be used for mapping; domain operations may not be called directly.
- `application` owns use cases, commands/results and ports. Spring service/transaction annotations are
  allowed; HTTP, Jackson, configuration classes and concrete adapter dependencies are not.
- `domain` owns framework-independent values, state rules and meaningful errors.
- `infrastructure` implements output ports and owns HTTP, serialization and local file formats.
- `config` composes the adapters and use cases. Bootstrap classes remain at each application root.

### Gateway

`GatewayProxyService` coordinates route selection, encryption, upstream exchange and decryption.
`RouteCatalog`, `RequestPayloads`, `UpstreamExchange`, `ResponseDecoder`, `CryptoModule` and
`ChecksumModule` describe the external capabilities it consumes. URI expansion and JSON parsing
belong to `JacksonRequestPayloads`; headers and Spring `RestClient` belong to the HTTP adapter.
`ProxyResponse` carries a numeric upstream status and body: preserving the upstream HTTP status is
an intentional contract of this transparent proxy, without depending on Spring `ResponseEntity`.
`GatewayProperties` implements the route catalog using immutable, duplicate-checked snapshots.

### Mock server

Controllers decode envelopes and explicitly map web DTOs to application commands and results back
to web DTOs. Each feature groups its small, related immutable contracts in one class.
`BusinessException.Kind` expresses invalid input, missing data or conflict; the web advice alone
maps these failures to HTTP 400/404/409.

Booking facades compose separate `domain.booking.BookingOperations` instances, preserving independent
organization state. The simulation owns its in-memory state and capacity checks; there is no external
repository or empty infrastructure package. Visit/support use cases retain their small in-memory
simulation state until a separate persistence boundary is needed. `Clock` and identifier suppliers
are injected, and capacity checks plus mutations retain the same synchronization boundary.

### Report server

Web and CLI publishing use the shared `RunPublisher` input port. `RunPublishService` depends on
`ReportParser` and `RunPublicationStore`; `SourceReportParser` selects the Karate/CATS parsers and
`FileRunPublicationStore` owns staging, copy/move and failure cleanup. Query services use `RunReader`
and `ContractCatalog`. Local `Path` remains an intentional port value because the use case publishes
local report directories; filesystem operations and source formats belong to adapters.

`ReportConfig` wires web dependencies. `CliConfig` composes the same use case without a web context;
CLI adapters receive that composition through a factory supplied by `ReportServerApplication`.
CLI command names, arguments and Gradle publishing tasks remain unchanged.

Mutable run/case models remain shared internal values for existing file compatibility; they have no
Jackson or Spring annotations. The storage adapter uses a private mapper copy and mixin to preserve
the seconds-only `meta.json` timestamp. HTTP responses and Thymeleaf view records own their separate
serialization/formatting contracts. Templates receive display labels and explicit fragment arguments.
Only `PageJson` output enters the two JSON script blocks through `th:utext`; its escaping remains tested.

### Architecture verification

Each module's `architectureTest` uses Java 21 `jdeps` to inspect compiled references, including method
bodies. It rejects cross-module implementation imports, legacy top-level packages, outward domain
and application dependencies, framework/serialization leakage, presentation access to output ports
or domain operations, and infrastructure access to application services. Transaction annotations
belong to application code. Karate is checked from its test classes because it has no production code.

The gate uses the JDK pinned by mise and adds no analysis dependency. `build-logic:check` exercises
allowed/forbidden policy fixtures and validates the Gradle plugin. The separate `verifyModuleGraph`
check compares documented Gradle edges with the evaluated build, rather than confusing package
architecture with deployment or HTTP dependencies.
