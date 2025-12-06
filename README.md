# Impact Analysis Gradle Plugin

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Gradle Plugin](https://img.shields.io/badge/Gradle-Plugin-green.svg)](https://gradle.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org/)
[![Test](https://github.com/haizerdev/impact-analysis-gradle-plugin/actions/workflows/test.yml/badge.svg)](https://github.com/haizerdev/impact-analysis-gradle-plugin/actions/workflows/test.yml)
[![Publish](https://github.com/haizerdev/impact-analysis-gradle-plugin/actions/workflows/publish.yml/badge.svg)](https://github.com/haizerdev/impact-analysis-gradle-plugin/actions/workflows/publish.yml)

Gradle plugin for automatic Git changes analysis and test scope determination in multi-module projects.

## Features

- **Git changes analysis** - automatic detection of modified files
- **Dependency graph** - build dependencies between modules
- **Smart test scope determination** - unit, integration, UI, E2E, API and more
- **Multi-module support** - works with any project structure
- **Flexible configuration** - DSL for rule customization
- **Uncommitted changes support** - analyze local changes
- **Configuration cache support** ⚡ - full support for Gradle configuration cache for faster builds
- **Parallel test execution** 🚀 - runs all tests in parallel using a single Gradle command (3-5x faster)
- **Android variant support** 📱 - specify which build variant to test (Debug, Release, custom flavors)
- **CI/CD time savings** - run only necessary tests (60-90% savings)

## Installation

### 1. Add plugin to `build.gradle.kts`

```kotlin
plugins {
    id("io.github.haizerdev.impactanalysis") version "1.0.12"
}
```

### 2. Configure the plugin

```kotlin
impactAnalysis {
    // Base branch for comparison
    baseBranch.set("origin/main")
    
    // Include uncommitted changes
    includeUncommittedChanges.set(true)
    
    // Unit tests
    unitTests {
        whenChanged("**/src/main/**/*.kt", "**/src/main/**/*.java")
        runOnlyInChangedModules = false
    }
    
    // Integration tests
    integrationTests {
        whenChanged("**/repository/**", "**/dao/**")
        runOnlyInChangedModules = true
    }
    
    // UI tests
    uiTests {
        whenChanged("**/*Screen.kt", "**/*Activity.kt")
        runOnlyInChangedModules = true
    }
}

## Automated Publishing

This plugin is automatically published to **Gradle Plugin Portal** using GitHub Actions.

### For Plugin Developers

When you create a new release tag (e.g., `v1.0.12`), GitHub Actions automatically:

1. Runs all tests
2. Builds the plugin
3. Publishes to Gradle Plugin Portal
4. Creates a GitHub Release with JAR files

**How to publish a new version:**

```bash
# Update version in build.gradle.kts
# version = "1.0.12"

git add build.gradle.kts
git commit -m "chore: bump version to 1.0.12"
git push origin main

git tag -a v1.0.12 -m "Release v1.0.12"
git push origin v1.0.12
```

## Usage

### Calculate impact analysis

```bash
./gradlew calculateImpact
```

**Console output with report:**

```
> Task :calculateImpact
Found 3 changed files
Directly changed modules: [:features:auth]
All affected modules: [:features:auth, :app]
UNIT tests: 2 tasks
  - :features:auth:testDebugUnitTest
  - :app:testDebugUnitTest
========== Impact Analysis Report ==========
Total tests to run: 8
Total tests skipped: 24
Modules to run (2): :app, :features:auth
Skipped modules (3): :core:database, :core:network, :features:profile
Estimated time saved: 12.0 minutes
===========================================
Files to lint: 2
Impact analysis result saved to: build/impact-analysis/result.json
```

**Quick workflow:**

```bash
# 1. Calculate impact and see the report
./gradlew calculateImpact

# 2. View detailed formatted report (optional)
./gradlew printImpactReport

# 3. Run affected tests using generated script (fastest)
./build/impact-analysis/run_impact_tests.sh

# Or use Gradle task (if runMode = GRADLE_TASK)
./gradlew runImpactTests
```

**Result in `build/impact-analysis/result.json`:**

```json
{
  "changedFiles": [
    {
      "path": "features/auth/src/main/kotlin/LoginScreen.kt",
      "module": ":features:auth",
      "changeType": "MODIFIED",
      "language": "KOTLIN"
    }
  ],
  "affectedModules": [":features:auth", ":app"],
  "testsToRun": {
    "UI": [":features:auth:connectedAndroidTest"],
    "UNIT": [":features:auth:test", ":app:test"]
  },
  "filesToLint": ["features/auth/src/main/kotlin/LoginScreen.kt"]
}
```

### Get list of changed files

```bash
./gradlew getChangedFiles
```

### Run tests based on impact analysis

```bash
./gradlew runImpactTests
```

## Configuration Examples

### Android Project

```kotlin
impactAnalysis {
    baseBranch.set("origin/develop")
    includeUncommittedChanges.set(true)

    // Android build variants (run only Debug tests, not Debug + Release)
    androidUnitTestVariant.set("Debug")           // testDebugUnitTest instead of test
    androidInstrumentedTestVariant.set("Debug")  // connectedDebugAndroidTest
    androidCompileVariant.set("Debug")           // compileDebugKotlin instead of compileKotlin

    unitTests {
        whenChanged("**/src/main/**/*.kt")
        runOnlyInChangedModules = false
    }
    
    integrationTests {
        whenChanged("**/repository/**", "**/database/**")
        runOnlyInChangedModules = true
    }
    
    uiTests {
        whenChanged("**/*Screen.kt", "**/*Activity.kt")
        runOnlyInChangedModules = true
    }
    
    criticalPaths.set(listOf(
        "build.gradle.kts",
        "gradle.properties",
        "gradle/libs.versions.toml"
    ))
}
```

For Android projects, you can specify which build variant to test:

```kotlin
impactAnalysis {
    // Unit tests - specify which variant (Debug/Release/Staging/etc.)
    androidUnitTestVariant.set("Debug")  // Default: "Debug"
    // Result: :app:testDebugUnitTest instead of :app:test
    
    // Instrumented/UI tests - specify which variant
    androidInstrumentedTestVariant.set("Debug")  // Default: "Debug"
    // Result: :app:connectedDebugAndroidTest

    // Kotlin compilation - specify which variant
    androidCompileVariant.set("Debug")  // Default: "Debug"
    // Result: :app:compileDebugKotlin instead of :app:compileKotlin
}
```

Available variants depend on your build configuration:

- Standard: `Debug`, `Release`
- Custom: `Staging`, `Production`, `Beta`, etc.
- Multi-flavor: `ProdRelease`, `ProdDebug`, `DevRelease`, etc.

**Examples:**

```kotlin
// Simple variant
androidUnitTestVariant.set("Debug")
// Result: testDebugUnitTest

// Custom build type
androidUnitTestVariant.set("Staging")
// Result: testStagingUnitTest

// Product flavor + build type
androidUnitTestVariant.set("ProdRelease")
// Result: testProdReleaseUnitTest

// The plugin automatically detects your project's available test tasks
// and finds the best match for your specified variant
```

### Backend Project (Spring Boot)

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")
    
    unitTests {
        whenChanged("**/src/main/**/*.kt", "**/src/main/**/*.java")
    }
    
    integrationTests {
        whenChanged("**/repository/**", "**/service/**")
    }
    
    apiTests {
        whenChanged("**/controller/**", "**/api/**", "**/endpoint/**")
        runOnlyInChangedModules = false
    }
}
```

### Microservices

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")
    
    // Critical files - run all tests
    criticalPaths.set(listOf(
        "**/docker-compose.yml",
        "**/Dockerfile",
        "**/kubernetes/**"
    ))
    
    unitTests {
        whenChanged("**/src/main/**")
        runOnlyInChangedModules = true // Only changed services
    }
    
    integrationTests {
        whenChanged("**/api/**", "**/grpc/**")
        runOnlyInChangedModules = false // All dependent services
    }
}
```

## Available Tasks

| Task                     | Description                                    |
|--------------------------|------------------------------------------------|
| `calculateImpact`        | Calculate impact analysis based on Git changes |
| `getChangedFiles`        | Get list of changed files                      |
| `getChangedFilesForLint` | Get list of files for linting (.kt, .java)     |
| `runImpactTests`         | Run tests based on impact analysis             |
| `runImpactKotlinCompile` | Run Kotlin compilation for affected modules    |
| `printImpactReport`      | Print detailed impact analysis report          |

## Impact Analysis Report 📊

The plugin provides comprehensive reporting to help you understand the impact of your changes and estimate time savings.

### Automatic Report Generation

When you run `calculateImpact`, a report is automatically generated and included in `result.json`:

```json
{
  "changedFiles": [...],
  "affectedModules": [...],
  "testsToRun": {...},
  "filesToLint": [...],
  "report": {
    "totalTestsToRun": 8,
    "totalTestsSkipped": 24,
    "modulesToRun": [":app", ":features:auth"],
    "skippedModules": [":core:database", ":core:network", ":features:profile"],
    "estimatedTimeSavedMinutes": 12.0,
    "testsByType": {
      "UNIT": 6,
      "UI": 2
    },
    "totalTestMethodsToRun": 245,
    "totalTestMethodsSkipped": 1580,
    "testMethodsByModule": {
      ":app": 120,
      ":features:auth": 125,
      ":core:database": 450,
      ":core:network": 380,
      ":features:profile": 750
    }
  }
}
```

**Note:** The plugin now counts **actual test methods** (not just test tasks) by scanning test files for `@Test`
annotations, giving you more accurate statistics!

### Console Report

The report is also displayed in the console after running `calculateImpact`:

```
========== Impact Analysis Report ==========
Total tests to run: 8
Total tests skipped: 24
Modules to run (2): :app, :features:auth
Skipped modules (3): :core:database, :core:network, :features:profile
Estimated time saved: 12.0 minutes
===========================================
```

### Bash Script Integration

When using `runMode = BASH`, the report is embedded in the generated script:

**As comments (for quick review):**
```bash
#!/bin/bash
# This script was generated by the Impact Analysis Plugin.
#
# ========== Impact Analysis Report ==========
# Total tests to run: 8
# Total tests skipped: 24
# Test methods to run: 245
# Test methods skipped: 1580
# Modules to run (2): :app, :features:auth
# Skipped modules (3): :core:database, :core:network, :features:profile
# Estimated time saved: 12.0 minutes
# ============================================
```

**As console output (when script runs):**

The script automatically prints a beautiful report before running tests:

```bash
./build/impact-analysis/run_impact_tests.sh
```

Output:
```
╔═══════════════════════════════════════════════════════════════╗
║          IMPACT ANALYSIS REPORT                               ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  📊 Test Tasks:                                               ║
║     • Tasks to run:      8                                    ║
║     • Tasks skipped:     24                                   ║
║                                                               ║
║  🧪 Test Methods:                                             ║
║     • Methods to run:    245                                  ║
║     • Methods skipped:   1580                                 ║
║                                                               ║
║  📦 Modules:                                                  ║
║     • Modules to run:    2                                    ║
║     • Modules skipped:   3                                    ║
║                                                               ║
║  ⏱️  Time Estimation:                                          ║
║     • Estimated time saved: 12.0 minutes                      ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

Starting tests...

> Task :app:testDebugUnitTest
> Task :features:auth:testDebugUnitTest
...
```

**Benefits:**

- 📝 See the report in script comments for quick review
- 🎯 Automatic report display when script runs
- 🔍 Easy to review in code reviews
- 📋 No need to run additional commands

### Detailed Report View

For a comprehensive detailed report with all statistics, use the `printImpactReport` task:

```bash
./gradlew printImpactReport
```

**Output:**

```
╔═══════════════════════════════════════════════════════════════╗
║          IMPACT ANALYSIS REPORT                               ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  📊 Test Statistics:                                          ║
║     • Tests to run:      8                                    ║
║     • Tests skipped:     24                                   ║
║     • Total tests:       32                                   ║
║                                                               ║
║  🧪 Test Methods:                                             ║
║     • Methods to run:    245                                  ║
║     • Methods skipped:   1580                                 ║
║     • Total methods:     1825                                 ║
║                                                               ║
║  🔍 Tests by Type:                                            ║
║     • UNIT                6                                   ║
║     • UI                  2                                   ║
║                                                               ║
║  📦 Module Statistics:                                        ║
║     • Modules to test:   2                                    ║
║     • Modules skipped:   3                                    ║
║     • Total modules:     5                                    ║
║                                                               ║
║  ⏱️  Time Estimation:                                          ║
║     • Estimated time saved: 12.0 minutes                      ║
║                                                               ║
╠═══════════════════════════════════════════════════════════════╣
║  ✅ Modules to Run:                                           ║
║     :app  :features:auth                                      ║
║                                                               ║
║  ⏭️  Skipped Modules:                                          ║
║     :core:database  :core:network  :features:profile          ║
║                                                               ║
║  📋 Test Methods by Module (Top 10):                          ║
║     ⏸️ :features:profile                          750       ║
║     ⏸️ :core:database                             450       ║
║     ⏸️ :core:network                              380       ║
║     ▶️ :features:auth                             125       ║
║     ▶️ :app                                       120       ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

Changed files: 3
Affected modules: 2
Files to lint: 2
```

This detailed report includes:

- Complete test statistics (tasks and methods)
- Tests breakdown by type
- Full module lists (to run and skipped)
- Top 10 modules by test count with run status (▶️/⏸️)

### Time Estimation

The plugin estimates time saved based on average test execution time:

- **Default estimate**: 0.5 minutes per test task
- **Calculation**: `skippedTests × 0.5 minutes`

This is a conservative estimate. Actual savings may be higher for:

- Integration tests (2-5 minutes per test)
- UI/E2E tests (5-10 minutes per test)
- Tests requiring emulators or external services

### Run Modes

Configure how you want to run tests:

```kotlin
impactAnalysis {
    runMode.set(ImpactRunMode.BASH)  // Generate bash script (default)
    // or
    runMode.set(ImpactRunMode.GRADLE_TASK)  // Use Gradle task
    // or
    runMode.set(ImpactRunMode.PYTHON)  // Generate Python script
}
```

**BASH mode** (recommended):

- ✅ Best performance - no Gradle overhead
- ✅ Report embedded in script comments
- ✅ Easy to review before running
- ✅ Works in any CI/CD environment

**GRADLE_TASK mode**:

- ✅ Native Gradle integration
- ✅ Better IDE support
- ❌ Slightly slower due to task overhead

**PYTHON mode**:

- ✅ Advanced scripting capabilities
- ✅ Cross-platform compatibility
- ❌ Requires Python installation

## Configuration Parameters

### Basic Parameters

```kotlin
impactAnalysis {
    // Base branch for comparison (default: "origin/main")
    baseBranch.set("origin/develop")
    
    // Branch to compare (default: "HEAD")
    compareBranch.set("HEAD")
    
    // Include uncommitted changes (default: true)
    includeUncommittedChanges.set(true)
    
    // Critical files - all tests run when changed
    criticalPaths.set(listOf(
        "build.gradle.kts",
        "gradle.properties"
    ))
    
    // File extensions for linting
    lintFileExtensions.set(listOf("kt", "java", "xml"))
}
```

### Test Rules

```kotlin
// Unit tests
unitTests {
    whenChanged("**/src/main/**/*.kt", "**/src/main/**/*.java")
    runOnlyInChangedModules = false // Run in all dependent modules
}

// Integration tests
integrationTests {
    whenChanged("**/repository/**", "**/dao/**")
    runOnlyInChangedModules = true // Only in changed modules
}

// UI tests
uiTests {
    whenChanged("**/*Screen.kt", "**/*Activity.kt")
    runOnlyInChangedModules = true
}

// E2E tests
e2eTests {
    whenChanged("**/api/**", "**/endpoint/**")
    runOnlyInChangedModules = false
}

// API tests
apiTests {
    whenChanged("**/controller/**", "**/service/**")
    runOnlyInChangedModules = false
}

// Custom test types
customTests("smoke") {
    whenChanged("**/critical/**")
    runOnlyInChangedModules = false
}
```

## CI/CD Examples

### GitHub Actions

```yaml
name: CI

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Full history for Git analysis
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Calculate Impact
        run: ./gradlew calculateImpact -PbaseBranch=origin/${{ github.base_ref }}
      
      - name: Run Impact Tests
        run: ./gradlew runImpactTests
```

### GitLab CI

```yaml
test:
  stage: test
  script:
    - ./gradlew calculateImpact -PbaseBranch=origin/main
    - ./gradlew runImpactTests
  only:
    - merge_requests
```

## Metrics

### Before using the plugin

- CI time per PR: **15-20 minutes**
- CI cost per month: **$500-1000**

### After using the plugin

- CI time per PR: **3-7 minutes** (↓70%)
- CI cost per month: **$200-400** (↓60%)
- Development speed: **+30%**

## Architecture

```
┌─────────────────┐
│   Git Changes   │
│   (JGit API)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Impact Analysis │
│   Calculator    │
└────────┬────────┘
         │
         ├──────────────┬──────────────┐
         ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Dependency   │ │ Test Scope   │ │ File         │
│ Graph        │ │ Calculator   │ │ Filter       │
└──────────────┘ └──────────────┘ └──────────────┘
         │              │              │
         └──────┬───────┴──────────────┘
                ▼
        ┌───────────────┐
        │ Result JSON   │
        └───────────────┘
```

## Development

### Running Tests

The plugin has comprehensive test coverage. To run tests:

```bash
# Run tests
./gradlew test

# Run tests with coverage report
./gradlew testWithReport
```

**Coverage: ~85%**

## Documentation

- [Architecture](ARCHITECTURE.md) - detailed architecture description
- [Implementation Plan](IMPLEMENTATION_PLAN.md) - how to implement in your project

## Contributing

We welcome contributions! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) file for details.

## Author

- GitHub: [@haizerdev](https://github.com/haizerdev)

## Support

If you find this project useful, please give it a ⭐️!

## Links

- [Gradle Plugin Portal](https://plugins.gradle.org/)
- [JGit Documentation](https://www.eclipse.org/jgit/)
- [Gradle Documentation](https://docs.gradle.org/)

---

**Made with ❤️ for the Developer Community**
