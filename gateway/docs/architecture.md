# Gateway — Module Architecture

For the full system overview and request flow, see [../../docs/architecture.md](../../docs/architecture.md).

## Package Layout

```text
src/main/java/com/example/gateway/
├── presentation/proxy/web/          # HTTP controller and exception advice
├── application/proxy/              # Proxy use case, ports, result and errors
├── domain/routing/model/           # Framework-independent route value
├── infrastructure/http/            # RestClient, headers, JSON and URI expansion
├── infrastructure/crypto/          # Base64 and SHA-256 adapters
└── config/                         # Route binding and bean composition
```

## Key Classes

| Class | Responsibility |
|---|---|
| `GenericGatewayController` | Entry point: maps `/{org}/{service}/{api}` to service call |
| `GatewayProxyService` | Orchestrates route lookup, encryption, upstream call, decryption |
| `GatewayProperties` | Binds immutable `gateway.apis[]` snapshots; rejects duplicate route keys |
| `CryptoModule` | Interface: `encrypt(key, plain)` / `decrypt(key, cipher)` |
| `ChecksumModule` | Interface: `checksum(data)` |
| `RouteNotFoundException` | Thrown when `(org, service, api, method)` has no matching route |

## Configuration Shape

```yaml
gateway:
  apis:
    - org: <institution>
      service: <service-name>
      api: <api-name>
      method: POST
      host: http://localhost:18080
      externalPath: /api/path
      key: <crypto-key>
```

## Upstream Request Format

```http
POST <host><externalPath>
X-Checksum: <sha256 of encrypted body>
X-Api-Key: <key>
X-Ins-Code: <org>
Content-Type: application/json

{"data": "<encrypted body>"}
```

## Error Handling

| Scenario | Behavior |
|---|---|
| Route not found | 404 via `RouteNotFoundException` |
| Upstream HTTP error | Propagate status code; log at WARN |
| Decryption failure / missing `$.data` | Log warning; return raw upstream response |
| Malformed request body | 400 |
