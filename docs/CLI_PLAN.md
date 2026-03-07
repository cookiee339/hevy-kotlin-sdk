# Hevy CLI — Implementation Plan

## Overview

A native CLI application that wraps the Hevy Kotlin SDK, compiled to a standalone binary via GraalVM `native-image`. Lives in the same repository as the SDK as a Gradle subproject.

## Goals

- Provide a fast, zero-dependency CLI for the Hevy API (`./hevy workouts list`)
- GraalVM native-image for instant startup and no JVM requirement
- JSON and table output formats
- API key from env var or flag
- Later: Claude Code skill for natural-language Hevy API interaction

## Non-Goals (for now)

- Write commands (create/update/delete) — follow-up PR
- MCP server — future phase
- Interactive/TUI mode

---

## Architecture

### Repository Structure (after restructuring)

```
hevy_kotlin_sdk/
  settings.gradle.kts              # include("sdk", "cli")
  build.gradle.kts                 # root: allprojects group/version only
  gradle.properties
  gradle/libs.versions.toml        # + clikt, graalvm-native, coroutines
  docs/
    CLI_PLAN.md                    # this file
  sdk/                             # existing SDK moved here
    build.gradle.kts               # existing KMP config (from root)
    src/
      commonMain/...
      commonTest/...
      jvmMain/...
      jsMain/...
      appleMain/...
  cli/                             # NEW — JVM target, compiled to native
    build.gradle.kts               # kotlin-jvm + graalvm native-image
    src/
      main/kotlin/com/hevy/cli/
        Main.kt                    # entry point
        HevyCli.kt                 # root Clikt command
        ClientProvider.kt          # API key resolution + HevyClient lifecycle
        OutputFormatter.kt         # JSON vs table output switching
        format/
          TablePrinter.kt          # column-aligned table rendering
        commands/
          WorkoutsCommand.kt       # workouts list|get|count|events
          RoutinesCommand.kt       # routines list|get
          ExercisesCommand.kt      # exercises list|get
          FoldersCommand.kt        # folders list|get
          HistoryCommand.kt        # history get <template-id>
          UserCommand.kt           # user info
      test/kotlin/com/hevy/cli/
        ClientProviderTest.kt
        OutputFormatterTest.kt
        format/TablePrinterTest.kt
        commands/
          WorkoutsCommandTest.kt
          RoutinesCommandTest.kt
          ExercisesCommandTest.kt
          FoldersCommandTest.kt
          HistoryCommandTest.kt
          UserCommandTest.kt
```

### CLI Command Tree

```
hevy [OPTIONS] COMMAND

Global Options:
  --api-key TEXT   Hevy API key (overrides HEVY_API_KEY env var)
  --json           Output raw JSON instead of formatted table
  --version        Show version and exit
  -h, --help       Show help and exit

Commands:
  workouts    Manage workouts
    list        List workouts [--page N] [--page-size N]
    get         Get workout by ID <id>
    count       Get total workout count
    events      List workout events [--page N] [--page-size N] [--since ISO]

  routines    Manage routines
    list        List routines [--page N] [--page-size N]
    get         Get routine by ID <id>

  exercises   Manage exercise templates
    list        List exercise templates [--page N] [--page-size N]
    get         Get exercise template by ID <id>

  folders     Manage routine folders
    list        List routine folders [--page N] [--page-size N]
    get         Get routine folder by ID <id>  (integer)

  history     Exercise history
    get         Get history for template <exercise-template-id>
                [--page N] [--page-size N]

  user        User profile
    info        Show authenticated user info
```

### Key Technology Choices

| Choice | Decision | Rationale |
|--------|----------|-----------|
| CLI framework | **Clikt 5.x** | Pure Kotlin, immutable option delegates, built-in `SuspendingCliktCommand`, lighter than picocli |
| Packaging | **GraalVM native-image** | Instant startup (~10ms vs ~1s JVM), no JVM dependency, single binary |
| HTTP engine | **Ktor CIO** (for CLI) | Pure Kotlin, no JNI/reflection — GraalVM-friendly. SDK uses OkHttp for general JVM, but CLI overrides via `HevyClientConfig.httpClient` |
| Serialization | **kotlinx.serialization** | Already used by SDK, compile-time (no reflection), GraalVM-safe |
| Build plugin | **org.graalvm.buildtools.native** | Official GraalVM Gradle plugin, integrates with reachability metadata repo |

---

## GraalVM Native Image Strategy

### Why GraalVM over Fat JAR

| Aspect | Fat JAR (Shadow) | GraalVM native-image |
|--------|-------------------|---------------------|
| Startup time | ~1-2 seconds | ~10-50 ms |
| Binary size | ~15 MB JAR + JVM | ~20-40 MB standalone |
| Runtime dependency | Requires JVM 11+ | None (self-contained) |
| Memory usage | ~100 MB+ (JVM heap) | ~20-30 MB |
| Distribution | `java -jar hevy.jar` | `./hevy` |
| Build time | ~5 seconds | ~2-5 minutes |
| CI complexity | Low | Medium (needs GraalVM in CI) |

### GraalVM Considerations

1. **Ktor CIO over OkHttp**: OkHttp uses JNI and reflection extensively. Ktor CIO is pure Kotlin and works cleanly with native-image. The CLI will provide its own `HttpClient(CIO)` via `HevyClientConfig`.

2. **kotlinx.serialization**: Uses compile-time code generation, no reflection at runtime. GraalVM-safe out of the box.

3. **Clikt**: Pure Kotlin, no reflection. GraalVM-safe.

4. **Coroutines**: Kotlin coroutines work with GraalVM but need the `kotlinx-coroutines-core` metadata. The GraalVM reachability metadata repository includes this.

5. **Reflection config**: Minimal — only needed if any library uses `Class.forName()` or similar. We'll use the GraalVM tracing agent during development to auto-generate configs if needed.

6. **Resource config**: Ktor may bundle resources (e.g., `META-INF/services`). The native-image plugin's `--initialize-at-build-time` and resource config will handle this.

### Gradle Configuration Sketch

```kotlin
// cli/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graalvm.native)
    application
}

application {
    mainClass.set("com.hevy.cli.MainKt")
}

dependencies {
    implementation(project(":sdk"))
    implementation(libs.clikt)
    implementation(libs.ktor.client.cio)       // GraalVM-friendly engine
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("com.hevy.cli.MainKt")
            imageName.set("hevy")
            buildArgs.addAll(
                "--no-fallback",
                "-H:+ReportExceptionStackTraces",
            )
        }
    }
    metadataRepository {
        enabled.set(true)  // use GraalVM reachability metadata repo
    }
}
```

### Build Commands

```bash
# Development: run directly on JVM (fast iteration)
./gradlew cli:run --args="workouts list"

# Build native binary
./gradlew cli:nativeCompile
# Output: cli/build/native/nativeCompile/hevy

# Run native binary
./cli/build/native/nativeCompile/hevy workouts list

# Run tests on JVM (native tests are slow, JVM tests are sufficient)
./gradlew cli:test
```

### CI: GraalVM Setup

```yaml
# In GitHub Actions
- uses: graalvm/setup-graalvm@v1
  with:
    java-version: '21'
    distribution: 'graalvm'
    github-token: ${{ secrets.GITHUB_TOKEN }}

- name: Build native image
  run: ./gradlew cli:nativeCompile

- name: Upload binary
  uses: actions/upload-artifact@v4
  with:
    name: hevy-cli-${{ runner.os }}
    path: cli/build/native/nativeCompile/hevy
```

Native binaries are platform-specific, so CI builds for Linux, macOS (x64 + arm64), and optionally Windows.

---

## Implementation Phases

### Phase 1: Multi-Module Restructuring

**Goal**: Convert single-module project to multi-module without breaking anything.

**Steps**:
1. `git mv src/ sdk/src/` and `git mv build.gradle.kts sdk/build.gradle.kts`
2. Create new root `build.gradle.kts` (minimal — `allprojects { group/version }`)
3. Update `settings.gradle.kts`: add `include("sdk", "cli")`
4. Remove `group`/`version` from `sdk/build.gradle.kts` (centralized in root)
5. Update `.github/workflows/` paths (`build/` → `sdk/build/`, task prefixes)
6. Verify: `./gradlew sdk:jvmTest`, `./gradlew sdk:publishToMavenLocal`

**Risk**: Low. Pure file moves + path updates. Use `git mv` to preserve history.

**Verification**:
- [ ] `./gradlew sdk:jvmTest` passes
- [ ] `./gradlew sdk:detekt` passes
- [ ] `./gradlew sdk:ktlintCheck` passes
- [ ] `./gradlew sdk:publishToMavenLocal` succeeds

---

### Phase 2: Version Catalog + CLI Skeleton

**Goal**: CLI subproject compiles and `./gradlew cli:run --args="--help"` works.

**Steps**:
1. Add to `gradle/libs.versions.toml`:
   - `clikt = "5.0.3"`
   - `graalvm-native = "0.10.4"` (or latest)
   - `ktor-client-cio` library entry
   - `kotlin-jvm` plugin alias
2. Create `cli/build.gradle.kts` (kotlin-jvm + graalvm-native + application)
3. Create `Main.kt` — delegates to `HevyCli().main(args)`
4. Create `HevyCli.kt` — root command with `--version`, `--help` only (no subcommands yet)

**Verification**:
- [ ] `./gradlew cli:build` compiles
- [ ] `./gradlew cli:run --args="--help"` shows help text

---

### Phase 3: Core Infrastructure

**Goal**: `ClientProvider`, `OutputFormatter`, `TablePrinter` ready for commands to use.

**Steps**:
1. **`ClientProvider.kt`**
   - Resolves API key: `--api-key` flag > `HEVY_API_KEY` env var > error with message
   - Creates `HevyClient` with Ktor CIO engine via `HevyClientConfig`
   - Returns `AutoCloseable` client (caller uses `.use {}`)

2. **`OutputFormatter.kt`**
   - Sealed interface with `JsonOutput` and `TableOutput` implementations
   - `JsonOutput`: uses `kotlinx.serialization.json.Json { prettyPrint = true }`
   - `TableOutput`: delegates to `TablePrinter`
   - Selected by `--json` flag on root command, passed via Clikt context

3. **`TablePrinter.kt`**
   - Takes headers + rows (List<List<String>>), computes column widths, prints padded
   - No ANSI colors (keep simple, works in all terminals)
   - Handles empty result sets gracefully ("No results found.")

4. **Tests** for all three

**SDK Change Required**: The `HevyClient.create(apiKey, engine)` factory is `internal`. Options:
- **(a)** Add a public constructor on `HevyClientConfig` that accepts an `HttpClient` directly — cleanest
- **(b)** Make the `create` companion function `public`
- Recommend **(a)**: add a `httpClient` parameter to `HevyClientConfig` (nullable, if provided skip factory creation)

**Verification**:
- [ ] `ClientProviderTest` passes (flag priority, missing key error)
- [ ] `OutputFormatterTest` passes (JSON format, table format)
- [ ] `TablePrinterTest` passes (alignment, empty, truncation)

---

### Phase 4: Read Commands

**Goal**: All read-only commands implemented and tested.

**Steps** (each command follows the same pattern):

1. **`WorkoutsCommand.kt`** — `workouts` subcommand group
   - `list`: `--page` (default 1), `--page-size` (default 10) → table: ID | Title | Start | End | Exercises
   - `get`: `<id>` argument → detailed single workout
   - `count`: no args → prints count
   - `events`: `--page`, `--page-size`, `--since` → table: Type | ID | Title | Updated At

2. **`RoutinesCommand.kt`** — `routines` subcommand group
   - `list`: paginated → table: ID | Title | Folder ID | Updated At
   - `get`: `<id>` → detailed routine

3. **`ExercisesCommand.kt`** — `exercises` subcommand group
   - `list`: paginated → table: ID | Title | Type | Primary Muscle | Equipment
   - `get`: `<id>` → detailed template

4. **`FoldersCommand.kt`** — `folders` subcommand group
   - `list`: paginated → table: ID | Index | Title | Created At
   - `get`: `<id>` (Int) → detailed folder

5. **`HistoryCommand.kt`** — `history` subcommand group
   - `get`: `<exercise-template-id>`, `--page`, `--page-size` → table per workout entry

6. **`UserCommand.kt`** — `user` subcommand group
   - `info`: no args → display user profile

7. **Wire all subcommands** into `HevyCli.kt`

8. **Integration tests** for each command using Clikt's `test()` + SDK `MockEngine`

**Verification**:
- [ ] Each command's integration test passes
- [ ] `./gradlew cli:run --args="workouts list --json"` works against real API
- [ ] `./gradlew cli:run --args="user info"` works against real API
- [ ] `--help` on every command/subcommand shows correct usage

---

### Phase 5: Error Handling

**Goal**: User-friendly error messages, proper exit codes, no stack traces.

**Steps**:
1. Catch `HevyException` subtypes in a top-level handler:
   - `Unauthorized` → "Error: Invalid API key." (exit 1)
   - `NotFound` → "Error: Resource not found." (exit 1)
   - `RateLimited` → "Error: Rate limited. Try again later." (exit 1)
   - `BadRequest` → "Error: {message}" (exit 1)
   - `ServerError` → "Error: Server error (HTTP {code})." (exit 1)
   - Network errors → "Error: Cannot reach API. Check your connection." (exit 1)
   - Missing API key → "Error: API key required. Set HEVY_API_KEY or use --api-key." (exit 2)
2. Ensure `HevyClient.close()` is called in all paths (use `.use {}`)
3. Test each error scenario

**Verification**:
- [ ] Missing API key → exit 2, clear message
- [ ] Bad API key → exit 1, "Invalid API key"
- [ ] Non-existent resource → exit 1, "Not found"

---

### Phase 6: GraalVM Native Image Build

**Goal**: `./gradlew cli:nativeCompile` produces a working `hevy` binary.

**Steps**:
1. Run GraalVM tracing agent to detect reflection/resource needs:
   ```bash
   ./gradlew cli:run -Pagent --args="workouts list"
   ```
   This generates config files in `cli/src/main/resources/META-INF/native-image/`
2. Review and commit generated configs (reflect-config.json, resource-config.json, etc.)
3. Test native binary against real API
4. If issues: add manual entries to reflect-config.json or switch problematic code
5. Optimize: `--gc=serial` for smaller binary if memory isn't a concern

**Fallback**: Keep `application` plugin so `./gradlew cli:run` always works on JVM. Native image is a distribution optimization, not a requirement for development.

**Verification**:
- [ ] `./gradlew cli:nativeCompile` succeeds
- [ ] `./hevy --help` prints help (native binary)
- [ ] `./hevy workouts list` returns data (native binary, real API key)
- [ ] `./hevy --json user info` returns valid JSON (native binary)
- [ ] Startup time < 100ms

---

### Phase 7: CI + Distribution

**Goal**: CI builds native binaries for Linux and macOS, attaches to GitHub releases.

**Steps**:
1. Update `.github/workflows/ci.yml`:
   - Add `cli:test` to test matrix
   - Add GraalVM setup + `cli:nativeCompile` job (matrix: linux, macos-arm64, macos-x64)
2. Update `.github/workflows/release.yml`:
   - Build native binaries for all platforms
   - Upload as release assets: `hevy-linux-amd64`, `hevy-macos-arm64`, `hevy-macos-x64`
3. Update root `README.md` with CLI section (installation, usage examples)
4. Create `cli/README.md` with full command reference

**Verification**:
- [ ] CI passes for SDK + CLI
- [ ] Release workflow produces platform binaries
- [ ] Downloaded binary works without JVM installed

---

### Phase 8: Claude Code Skill (post-CLI)

**Goal**: Skill file that teaches Claude how to use the `hevy` CLI.

**Steps**:
1. Create `~/.claude/skills/hevy.md` documenting:
   - All CLI commands with examples
   - Common workflows ("show my recent workouts", "what exercises target chest")
   - How to interpret output
   - Error troubleshooting
2. Test skill activation with natural language prompts

This phase is separate and depends on the CLI being installed and working.

---

## Exit Criteria

- [ ] Multi-module build works (`sdk:jvmTest` + `cli:test` pass)
- [ ] All read commands implemented with tests (80%+ coverage)
- [ ] Error handling covers all `HevyException` subtypes
- [ ] GraalVM native binary builds and runs on macOS
- [ ] `./hevy --help` shows full command tree
- [ ] `./hevy workouts list` returns formatted table
- [ ] `./hevy --json workouts list` returns valid JSON
- [ ] CI builds native binaries
- [ ] README documents CLI installation and usage

## Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| GraalVM + Ktor CIO compatibility | Medium | Use CIO (pure Kotlin, no JNI). Fallback: add reflect-config for OkHttp |
| KMP→JVM cross-project dependency | Low | Gradle resolves JVM variant automatically for KMP projects |
| Native image build time in CI | Low | Cache GraalVM installation, only build on release tags |
| SDK `internal` visibility for testing | Low | Make `HevyClientConfig` accept optional `HttpClient` parameter |
| Platform-specific binary distribution | Low | CI matrix for linux/macos, document `brew` or direct download |
