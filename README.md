# Hevy Kotlin SDK

Kotlin Multiplatform SDK for the [Hevy](https://www.hevyapp.com/) fitness app API.

## Platforms

| Target | Engine |
|--------|--------|
| JVM | OkHttp |
| JS (Browser & Node) | Ktor JS |
| iOS (arm64, simulatorArm64, x64) | Darwin (URLSession) |
| macOS (arm64, x64) | Darwin (URLSession) |

## Installation

### Gradle (Kotlin DSL)

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.hevy:hevy-kotlin-sdk:0.1.0")
}
```

### Maven

```xml
<dependency>
    <groupId>com.hevy</groupId>
    <artifactId>hevy-kotlin-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```kotlin
import com.hevy.sdk.HevyClient

val hevy = HevyClient(apiKey = "your-api-key")

// List workouts (paginated)
val page = hevy.workouts.list(page = 1, pageSize = 5)
println("Page ${page.page} of ${page.pageCount}: ${page.items.size} workouts")

// Auto-paginate through all workouts
import com.hevy.sdk.common.paginate
paginate { p -> hevy.workouts.list(page = p, pageSize = 10) }
    .collect { workout -> println(workout.title) }

// Get a single workout
val workout = hevy.workouts.get(workoutId = "abc-123")

// Create a workout
import com.hevy.sdk.model.workout.*
import com.hevy.sdk.model.common.SetType

val request = CreateWorkoutRequest(
    workout = CreateWorkoutRequest.CreateWorkoutBody(
        title = "Push Day",
        exercises = listOf(
            CreateWorkoutExercise(
                exerciseTemplateId = "template-id",
                sets = listOf(
                    CreateWorkoutSet(type = SetType.NORMAL, weightKg = 80.0, reps = 8),
                    CreateWorkoutSet(type = SetType.NORMAL, weightKg = 80.0, reps = 6),
                ),
            ),
        ),
    ),
)
val created = hevy.workouts.create(request)

// Don't forget to close when done
hevy.close()
```

## API Domains

### Workouts

```kotlin
hevy.workouts.list(page, pageSize)       // Paginated list
hevy.workouts.create(request)            // Create a workout
hevy.workouts.get(workoutId)             // Get by ID
hevy.workouts.update(workoutId, request) // Update by ID
hevy.workouts.count()                    // Total count
hevy.workouts.events(page, pageSize, since) // Change events
```

### Routines

```kotlin
hevy.routines.list(page, pageSize)       // Paginated list
hevy.routines.create(request)            // Create a routine
hevy.routines.get(routineId)             // Get by ID
hevy.routines.update(routineId, request) // Update by ID
```

### Exercise Templates

```kotlin
hevy.exerciseTemplates.list(page, pageSize) // Paginated (up to 100/page)
hevy.exerciseTemplates.create(request)      // Create custom template
hevy.exerciseTemplates.get(templateId)      // Get by ID
```

### Routine Folders

```kotlin
hevy.routineFolders.list(page, pageSize)  // Paginated list
hevy.routineFolders.create(request)       // Create a folder
hevy.routineFolders.get(folderId: Int)    // Get by ID (Int, not String)
```

### Exercise History

```kotlin
hevy.exerciseHistory.getByTemplateId(
    exerciseTemplateId = "template-id",
    startDate = "2024-01-01T00:00:00Z", // optional
    endDate = "2024-12-31T23:59:59Z",   // optional
)
```

### User Info

```kotlin
val user = hevy.users.getInfo()
println("${user.username} - ${user.accountCreated}")
```

## Auto-Pagination

Use the `paginate()` helper to stream through all pages as a Kotlin `Flow`:

```kotlin
import com.hevy.sdk.common.paginate

val allTemplates = paginate { page ->
    hevy.exerciseTemplates.list(page = page, pageSize = 100)
}.toList()
```

## Configuration

### Custom Base URL

```kotlin
import com.hevy.sdk.HevyClientConfig

val config = HevyClientConfig(
    apiKey = "your-api-key",
    baseUrl = "https://custom-proxy.example.com",
)
val hevy = HevyClient(config)
```

### Bring Your Own HttpClient

```kotlin
import io.ktor.client.HttpClient

val myClient = HttpClient {
    // your custom configuration
}
val config = HevyClientConfig(
    apiKey = "your-api-key",
    httpClient = myClient,
)
val hevy = HevyClient(config)
```

## Error Handling

All API errors throw a sealed `HevyException`:

```kotlin
import com.hevy.sdk.error.HevyException

try {
    hevy.workouts.get("nonexistent-id")
} catch (e: HevyException.NotFound) {
    println("Workout not found")
} catch (e: HevyException.Unauthorized) {
    println("Invalid API key")
} catch (e: HevyException.RateLimited) {
    println("Rate limited, retry after ${e.retryAfterSeconds}s")
} catch (e: HevyException.BadRequest) {
    println("Bad request: ${e.message}")
} catch (e: HevyException.ServerError) {
    println("Server error (${e.statusCode}): ${e.message}")
} catch (e: HevyException.NetworkError) {
    println("Network error: ${e.message}")
} catch (e: HevyException.Unknown) {
    println("Unexpected status ${e.statusCode}: ${e.message}")
}
```

## Development

### Prerequisites

- JDK 17+
- Gradle 9.3+ (wrapper included)

<!-- AUTO-GENERATED from build.gradle.kts — do not edit manually -->
### Gradle Commands

| Command                              | Description                                                             |
|--------------------------------------|-------------------------------------------------------------------------|
| `./gradlew sdk:build`                    | Compile all KMP targets                                                 |
| `./gradlew sdk:jvmTest`                  | Run tests on JVM                                                        |
| `./gradlew sdk:jsNodeTest`               | Run tests on Node.js                                                    |
| `./gradlew sdk:macosArm64Test`           | Run tests on macOS (Apple Silicon)                                      |
| `./gradlew sdk:iosSimulatorArm64Test`    | Run tests on iOS Simulator                                              |
| `./gradlew sdk:allTests`                 | Run tests on all platforms                                              |
| `./gradlew sdk:detekt`                   | Static analysis (detekt)                                                |
| `./gradlew sdk:ktlintCheck`              | Code style check (ktlint)                                               |
| `./gradlew sdk:ktlintFormat`             | Auto-format code (ktlint)                                               |
| `./gradlew sdk:publishToMavenLocal`      | Publish to local Maven repository                                       |
| `./gradlew sdk:publish`                  | Publish to GitHub Packages (requires `GITHUB_ACTOR` and `GITHUB_TOKEN`) |
<!-- END AUTO-GENERATED -->

### CI / Release

- **CI** runs on every push/PR to `main`: lint (detekt + ktlint), then tests on JVM, JS, and Apple targets in parallel.
- **Release** is triggered by pushing a `v*` tag. It runs CI first, then publishes all targets to GitHub Packages.

## License

MIT
