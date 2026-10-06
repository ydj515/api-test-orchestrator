# api-test-orchestrator

OpenAPI/Swagger 계약을 기준으로 Gateway, mock REST API, Karate E2E, CATS fuzzing, 통합 리포트 발행까지 한 번에 검증하는 API 테스트 오케스트레이션 샘플입니다.

## 문서

| 주제 | 문서 |
|---|---|
| 프로젝트 구조와 아키텍처 | [docs/architecture.md](docs/architecture.md) |
| 개발 환경과 mise 태스크 | [docs/development.md](docs/development.md) |
| 테스트 실행 | [docs/testing.md](docs/testing.md) |
| OpenAPI 기반 Karate 생성 규칙 | [docs/karate-generation.md](docs/karate-generation.md) |
| CATS 리포트와 smoke/full 실행 가이드 | [docs/cats-report-guide.md](docs/cats-report-guide.md) |
| OpenAPI 계약 문서 | [docs/openapi/README.md](docs/openapi/README.md) |
| 보안과 설정 | [docs/security.md](docs/security.md) |

## 빠른 시작

mock, gateway, report-server를 각각 실행한 뒤 루트에서 통합 테스트를 실행합니다.

```bash
mise run mock:run
mise run gateway:run
mise run report:run
```

```bash
# 전체 기관/전체 서비스에 대해 Karate + CATS 실행 후 레포트 발행
mise run test

# Karate만 실행
SOURCE=karate mise run test

# CATS만 실행
SOURCE=cats mise run test

# 특정 서비스만 실행
ORG=orgB SERVICE=visit mise run test

# 특정 API만 실행
ORG=orgA SERVICE=reservation API=createReservation SOURCE=karate mise run test
```

레포트 UI는 아래 주소에서 확인합니다.

```text
http://localhost:48080/
```

## OpenAPI 기반 Karate 테스트

Karate feature는 `docs/openapi/mock-rest-api-server/*.yaml` Swagger 문서를 기준으로 생성합니다. mock 서버 구현 코드를 테스트 입력값의 근거로 사용하지 않고, Swagger의 request schema에 정의된 `required`, `enum`, `format`, `minimum`, `maximum`, `example` 값을 기준으로 정상/negative case를 만듭니다.

생성 규칙과 실행 예시는 [docs/karate-generation.md](docs/karate-generation.md)를 기준으로 관리합니다.

## 리포트 UI 예시

아래 이미지는 Playwright로 캡처한 실제 `report-server` 화면입니다. 최신 Swagger 기반 Karate 실행 결과가 서비스별 PASS 상태와 케이스 수로 표시됩니다.

### 서비스 목록

![API Test Report 서비스 목록](docs/report-server/service-list.png)

### 서비스별 배치 이력

![orgB visit 배치 이력](docs/report-server/visit-history.png)

### Karate 실행 상세

![orgB visit Karate 실행 상세](docs/report-server/visit-detail.png)

### OpenAPI negative case 상세

![OpenAPI negative case 상세](docs/report-server/openapi-negative-cases.png)

## 리포트 산출물 경로

| 경로 | 역할 |
|---|---|
| `output/` | 수동 검증용 실행 로그와 임시 산출물 보관 |
| `karate-tests/build/karate-reports/karate-reports/` | Karate raw 리포트 |
| `cats-report/` | CATS raw 리포트 |
| `report-server/data/runs/` | `report-server`가 읽는 최종 발행 저장소 |

Karate와 CATS raw 리포트는 publish 스크립트를 거쳐 `report-server/data/runs/{runId}/` 아래로 복사됩니다.

## mise 환경과 초기 설정

`mise.toml`은 도구·공통 task, `mise.dev.toml`/`mise.prod.toml`은 공유 환경 선택을 담당한다.

```bash
mise trust ./mise.toml   # task와 overlay를 검토한 뒤 신뢰
mise run bootstrap     # 고정된 버전의 도구 설치 후 의존성 준비
mise run config:check  # task 참조·순환 검사, 앱 실행 없음
mise run verify        # 프로젝트 검증 (Docker 등 기존 검증 전제는 유지)
mise -E dev run verify
```

- 기본 실행은 `APP_ENV=local`, `-E dev`는 개발 overlay, `-E prod`는 운영 설정 선택이다. 환경 선택 자체가 배포나 서비스 시작을 수행하지 않는다.
- 개인 개발 설정은 `mise.dev.local.toml.example`을 검토해 `mise.dev.local.toml`로 복사한다. `.env.dev.local`을 만든 뒤 `env._.file`을 활성화하면 dev에서만 읽는다. 기존 개인 파일을 덮어쓰지 않는다.
- `mise.local.toml`은 **모든 환경**에서 로드된다. prod checkout에 개인 override나 개발 dotenv를 두지 않는다. `-E local`은 사용하지 않는다.
- `APP_ENV`는 공통 식별자다. Spring 실행 task의 프로파일은 해당 task에서 매핑하며, 존재하지 않는 운영 설정을 자동 생성하지 않는다. prod 선택만으로 기존 개발용 앱이 운영 준비를 마친 것은 아니다.
- mise는 개발 도구의 정확한 버전 고정과 프로필 분리에 사용한다. `[settings] lockfile = false`로 도구 lock 생성을 끄고 `mise.lock`은 관리하지 않는다. 공통 표준의 mise lock 지침보다 이 저장소의 정책을 우선한다. 설치 파일까지 고정해야 하는 요구가 생기면 다시 도입한다.
- 도구 버전은 `mise.toml`의 `[tools]`에서 관리하며 dev/prod에서도 같은 버전을 사용한다. 로컬과 CI는 `mise install` 또는 같은 정확한 버전의 setup action으로 도구를 준비한다. `package-lock.json`, `pnpm-lock.yaml`, `uv.lock`, Gradle lock 등 애플리케이션 의존성 잠금과 검증 옵션은 유지한다.
- 지원 OS는 각 도구와 실행 스크립트의 호환성에 따른다. mise lock을 사용하지 않는다고 Windows 실행까지 보장되는 것은 아니다.
- `mise run bootstrap`은 프로젝트 초기 설정이다. OS package·dotfile·서비스를 관리하는 `mise bootstrap`은 개인 머신 설정에서 별도로 채택한다.

하위 설정: `gateway`, `mock-rest-api-server`, `report-server`, `karate-tests`. 각 앱 디렉터리에서 같은 명령을 실행한다. 루트 bootstrap/verify가 하위 앱 전체를 자동 실행하지는 않는다.
