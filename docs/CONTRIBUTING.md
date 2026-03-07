# Contributing to Hevy Kotlin SDK

## Prerequisites

- JDK 17+
- macOS recommended for running Apple-target tests (iOS/macOS)

## Setup

```bash
git clone https://github.com/hevy-sdk/hevy-kotlin-sdk.git
cd hevy-kotlin-sdk
./gradlew sdk:build
```

The Gradle wrapper is included — no separate Gradle installation required.

## Available Commands

<!-- AUTO-GENERATED from build.gradle.kts — do not edit manually -->
| Command                                  | Description                                |
|------------------------------------------|--------------------------------------------|
| `./gradlew sdk:build`                    | Compile all KMP targets                    |
| `./gradlew sdk:jvmTest`                  | Run tests on JVM                           |
| `./gradlew sdk:jsNodeTest`               | Run tests on Node.js                       |
| `./gradlew sdk:macosArm64Test`           | Run tests on macOS (Apple Silicon)         |
| `./gradlew sdk:iosSimulatorArm64Test`    | Run tests on iOS Simulator                 |
| `./gradlew sdk:allTests`                 | Run tests on all platforms                 |
| `./gradlew sdk:detekt`                   | Static analysis (detekt)                   |
| `./gradlew sdk:ktlintCheck`              | Code style check (ktlint)                  |
| `./gradlew sdk:ktlintFormat`             | Auto-format code (ktlint)                  |
| `./gradlew sdk:publishToMavenLocal`      | Publish to local Maven repository          |
<!-- END AUTO-GENERATED -->

## Testing

All tests live in `sdk/src/commonTest/` and run on every KMP target via Ktor's `MockEngine`.

```bash
# Quick feedback loop (JVM only)
./gradlew sdk:jvmTest

# Full cross-platform suite
./gradlew sdk:allTests
```

### Writing Tests

- Place tests in `sdk/src/commonTest/kotlin/com/hevy/sdk/` mirroring the main source layout.
- Use `ktor-client-mock` to stub HTTP responses — no real network calls in tests.
- Follow the existing pattern: each API test creates a `HevyClient` via `HevyClient.create(apiKey, mockEngine)`.

## Code Style

The project enforces two complementary style tools:

- **detekt** — static analysis for Kotlin code smells.
- **ktlint** — code formatting (Kotlin style guide).

Both run in CI. Fix issues locally before pushing:

```bash
./gradlew sdk:detekt sdk:ktlintCheck   # check
./gradlew sdk:ktlintFormat              # auto-fix formatting
```

## Project Structure

```
sdk/
└── src/
    ├── commonMain/kotlin/com/hevy/sdk/
    │   ├── HevyClient.kt              # Entry point
    │   ├── HevyClientConfig.kt        # Configuration
    │   ├── common/                     # Page, pagination, HTTP factory, validation
    │   ├── error/                      # HevyException sealed hierarchy
    │   ├── model/                      # Domain models (workout, routine, exercise, etc.)
    │   └── api/                        # Domain API classes (one per domain)
    ├── commonTest/                     # All tests (MockEngine-based)
    ├── jvmMain/                        # OkHttp engine
    ├── jsMain/                         # JS engine
    └── nativeMain/                     # Darwin engine (iOS/macOS)
```

## CI Pipeline

CI runs automatically on push/PR to `main`:

1. **Lint** — `detekt` + `ktlintCheck`
2. **Test (JVM)** — `jvmTest`
3. **Test (JS)** — `jsNodeTest`
4. **Test (Apple)** — `macosArm64Test` + `iosSimulatorArm64Test` (runs on macOS runner)

## Releasing

Releases are triggered by pushing a version tag:

```bash
git tag v0.2.0
git push origin v0.2.0
```

The release workflow runs CI first, then publishes all KMP targets to GitHub Packages.

### Environment Variables for Publishing

| Variable       | Required | Description                    |
|----------------|----------|--------------------------------|
| `GITHUB_ACTOR` | Yes      | GitHub username (set by CI)    |
| `GITHUB_TOKEN` | Yes      | GitHub token with `packages:write` (set by CI) |

## Pull Request Checklist

- [ ] Tests pass (`./gradlew sdk:jvmTest` at minimum)
- [ ] No lint errors (`./gradlew sdk:detekt sdk:ktlintCheck`)
- [ ] New public API has KDoc
- [ ] README updated if API surface changed
