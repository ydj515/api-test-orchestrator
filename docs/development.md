# Development Guide

## Runtime Environment

This project uses [mise](https://mise.jdx.dev) to pin tool versions across all modules.

| File | Scope |
|---|---|
| `mise.toml` (root) | Java 21.0.2 + aggregate tasks |
| `gateway/mise.toml` | Java 21.0.2 + gateway tasks |
| `mock-rest-api-server/mise.toml` | Java 21.0.2 + mock tasks |

**First-time setup:**
```shell
# From repository root — installs Java 21.0.2 for all modules
mise install
```

If mise is not available, ensure Java 21 is on `PATH` and use the Gradle wrapper directly.

## Mise Tasks

### From the repository root

| Command | Description |
|---|---|
| `mise run gateway:run` | Start gateway service (port 28080) |
| `mise run mock:run` | Start mock-rest-api-server (port 18080) |
| `mise run report:run` | Start report-server (port 48080) |
| `mise run build` | Build and verify all modules |
| `mise run unit:test` | Run all module checks without external servers |
| `SOURCE=all mise run test` | Run Karate + CATS and publish reports |
| `SOURCE=karate mise run test` | Run Karate and publish reports |
| `SOURCE=cats mise run test` | Run CATS and publish reports |
| `mise run clean` | Clean all modules |

Build and unit test tasks use the root Gradle lifecycle. Root `check` explicitly depends on all four module checks.
The E2E `test` task attempts to publish a report after each selected tool runs, then returns the original test failure if one occurred.

### From a module directory

```shell
cd gateway
mise run run       # start this service
mise run build     # build this module
mise run test      # test this module
mise run clean     # clean this module
```

Same commands apply inside `mock-rest-api-server/`.

## Running Both Services Together

Gateway and mock server must both be running for end-to-end tests. The report server is needed to browse published history. Open three terminals:

```shell
# terminal 1
mise run mock:run

# terminal 2
mise run gateway:run

# terminal 3
mise run report:run
```

Then run tests from the repository root:

```shell
SOURCE=all mise run test
ORG=orgA SERVICE=reservation API=createReservation SOURCE=karate mise run test
ORG=orgA SERVICE=reservation API=listResources CATS_PROFILE=smoke SOURCE=cats mise run test
ORG=orgB SERVICE=visit API=listSites SOURCE=cats mise run test
```

## Gradle Wrapper (fallback)

If mise is unavailable, run from the module directory:

```shell
cd gateway
./gradlew bootRun     # start
./gradlew test        # test
./gradlew build       # build
./gradlew clean       # clean
```

Always use `./gradlew` (wrapper) rather than a system-level `gradle` installation to ensure consistent Gradle version.

## Shared Build Conventions

`build-logic` supplies `orchestrator.java-conventions`: Java 21 toolchains and release targets,
UTF-8 compilation, and JUnit Platform. Each module owns its repositories and dependencies.
`gradle/libs.versions.toml` centralizes existing library/plugin coordinates without changing versions.
Standalone module settings import the same catalog and included build used by the root.
All wrappers use Gradle 8.14.3 with the official distribution SHA-256 checksum.

```shell
mise exec -- ./gradlew check           # local checks for every module; no running servers required
mise exec -- ./gradlew build           # check and package all modules
mise exec -- ./gradlew -p gateway check # standalone module entry point
```

Karate's `test` task validates runner selection without HTTP calls. Its `e2eTest` task runs
scenarios against the configured gateway and never treats a previous run as up to date.
`mise run karate:run` and `scripts/run-karate.sh` use `e2eTest`.
`ORG` selects the HTTP route through `karate-config.js`; `SERVICE` and `API` select feature tags.
An empty scenario selection fails rather than silently passing.

The report server retains Spring Boot 3.5.0; gateway/mock retain 3.5.8. Version alignment,
additional third-party static-analysis plugins and dependency-verification rollout require separate dependency
review. This refactoring does not claim a dependency-security audit or change resolved library versions.

`check` also runs bytecode architecture checks, the documented module graph check and build-plugin
validation. Configuration cache storage and reuse are supported for this verification entry point.
The architecture gate uses the Java 21 toolchain's `jdeps`, not a separately installed executable.
