# Hevy Kotlin Multiplatform SDK — Implementation Plan

## Context

Create a Kotlin Multiplatform SDK for the Hevy fitness app API based on the OpenAPI spec at `hevy_openapi.yaml`. The SDK will use Ktor Client, kotlinx.serialization, target JVM/JS/Native, and be publishable to Maven.

## API Surface (15 endpoints, 6 domains)

| Domain | Endpoints |
|---|---|
| Workouts | GET list, POST create, GET count, GET events, GET by ID, PUT update |
| Routines | GET list, POST create, GET by ID, PUT update |
| Exercise Templates | GET list, POST create, GET by ID |
| Routine Folders | GET list, POST create, GET by ID |
| Exercise History | GET by template ID (with date filter) |
| Users | GET info |

Auth: `api-key` header (UUID).

## Architecture

**Domain-grouped sub-clients:**
```kotlin
val hevy = HevyClient(apiKey = "uuid")
hevy.workouts.list(page = 1, pageSize = 5)
hevy.routines.get(routineId = "abc")
hevy.exerciseTemplates.listAll()  // auto-paginating Flow
```

**Key decisions:**
- Single KMP module (API is small enough, no multi-module overhead)
- Separate response models vs request models (different shapes)
- `@SerialName("snake_case")` on all properties (explicit, self-documenting)
- Sealed class `WorkoutEvent` for polymorphic Updated/Deleted events
- `Page<T>` wrapper + `Flow<T>` auto-pagination via `paginate()` helper
- Sealed `HevyException` hierarchy mapped from HTTP status codes
- Users can bring their own `HttpClient` or use the auto-configured default

## File Structure

```
hevy_kotlin_sdk/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── src/
│   ├── commonMain/kotlin/com/hevy/sdk/
│   │   ├── HevyClient.kt                    # Entry point, composes domain APIs
│   │   ├── HevyClientConfig.kt              # Config data class
│   │   ├── common/
│   │   │   ├── Page.kt                      # Pagination wrapper
│   │   │   ├── PaginationUtils.kt           # Flow-based auto-pagination
│   │   │   ├── HttpClientFactory.kt         # Ktor setup (auth, JSON, error handling)
│   │   │   ├── ApiConstants.kt              # Base URL, defaults
│   │   │   └── Validation.kt               # Input validation
│   │   ├── error/
│   │   │   ├── HevyException.kt             # Sealed exception hierarchy
│   │   │   ├── ErrorResponse.kt             # API error body model
│   │   │   └── ResponseExtensions.kt        # HTTP status → exception mapping
│   │   ├── model/
│   │   │   ├── common/                      # SetType, ExerciseType, MuscleGroup, EquipmentCategory enums
│   │   │   ├── workout/                     # Workout, WorkoutExercise, WorkoutSet, WorkoutEvent, CreateWorkoutRequest
│   │   │   ├── routine/                     # Routine, RoutineExercise, RoutineSet, RepRange, Create/UpdateRoutineRequest
│   │   │   ├── exercise/                    # ExerciseTemplate, CreateExerciseTemplateRequest
│   │   │   ├── history/                     # ExerciseHistoryEntry
│   │   │   ├── folder/                      # RoutineFolder, CreateRoutineFolderRequest
│   │   │   └── user/                        # UserInfo
│   │   └── api/
│   │       ├── WorkoutsApi.kt               # 6 endpoints
│   │       ├── RoutinesApi.kt               # 4 endpoints
│   │       ├── ExerciseTemplatesApi.kt       # 3 endpoints
│   │       ├── RoutineFoldersApi.kt          # 3 endpoints
│   │       ├── ExerciseHistoryApi.kt         # 1 endpoint
│   │       └── UsersApi.kt                  # 1 endpoint
│   ├── commonTest/kotlin/com/hevy/sdk/      # All tests (mock engine, serialization, API)
│   ├── jvmMain/                             # OkHttp engine dependency only
│   ├── jsMain/                              # JS engine dependency only
│   └── nativeMain/                          # Darwin engine dependency only
```

## Dependencies

| Library | Version |
|---|---|
| Kotlin | 2.1.10 |
| Ktor | 3.1.1 |
| kotlinx.serialization | 1.8.0 |
| kotlinx.coroutines | 1.10.1 |
| Ktor MockEngine (test) | 3.1.1 |

## Implementation Phases

### Phase 1: Project Skeleton
- `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`
- KMP targets: JVM, JS (IR), iosArm64, iosSimulatorArm64, iosX64, macosArm64, macosX64
- Maven publishing config
- `ApiConstants.kt`
- Verify `./gradlew build` compiles

### Phase 2: Core Infrastructure
- `HevyClientConfig`, `HttpClientFactory`, `Page<T>`, `PaginationUtils`, `Validation`
- `HevyException` sealed hierarchy, `ErrorResponse`, `ResponseExtensions`
- Tests for all core utilities

### Phase 3: Enums
- `SetType`, `ExerciseType`, `MuscleGroup`, `EquipmentCategory`
- Serialization round-trip tests

### Phase 4: Workout Domain
- Models: `Workout`, `WorkoutExercise`, `WorkoutSet`, `WorkoutEvent` (sealed), `CreateWorkoutRequest`
- `WorkoutsApi` (all 6 endpoints)
- Tests with MockEngine

### Phase 5: Routine Domain
- Models: `Routine`, `RoutineExercise`, `RoutineSet`, `RepRange`, `Create/UpdateRoutineRequest`
- `RoutinesApi` (4 endpoints)
- Tests

### Phase 6: Exercise Templates
- Models + `ExerciseTemplatesApi` (3 endpoints) + tests

### Phase 7: Remaining Domains
- Routine folders, exercise history, users
- Models + APIs + tests

### Phase 8: Main Client Assembly
- `HevyClient.kt` composing all domain APIs
- Integration test

### Phase 9: Polish
- KDoc on all public types
- Maven publishing metadata (group, artifact, license, SCM)
- `README.md` with usage examples
- Full test suite verification

## API Quirks to Handle

1. `GET /v1/routines/{id}` wraps response in `{"routine": ...}` — other GET-by-ID endpoints don't
2. `GET /v1/user/info` wraps in `{"data": ...}` — unwrap to `UserInfo`
3. `POST /v1/exercise_templates` returns only `{"id": 123}` — not the full template
4. Workout sets, routine sets, and request sets have different shapes — separate types needed
5. PUT routine has no `folder_id`; POST routine does — separate request types
6. Exercise template `pageSize` max is 100 (vs 10 for other endpoints)

## Verification

1. `./gradlew build` — compiles for all KMP targets
2. `./gradlew allTests` — runs commonTest suite on all platforms
3. `./gradlew publishToMavenLocal` — verifies publication artifacts
4. Manual smoke test: create `HevyClient`, call `workouts.list()` against real API with a test key
