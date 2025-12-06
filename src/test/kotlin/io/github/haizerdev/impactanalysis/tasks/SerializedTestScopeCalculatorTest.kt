package io.github.haizerdev.impactanalysis.tasks

import io.github.haizerdev.impactanalysis.extension.TestTypeRule
import io.github.haizerdev.impactanalysis.model.ChangedFile
import io.github.haizerdev.impactanalysis.model.ChangeType
import io.github.haizerdev.impactanalysis.model.FileLanguage
import io.github.haizerdev.impactanalysis.model.TestType
import io.github.haizerdev.impactanalysis.scope.ImpactAnalysisConfig
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for SerializedTestScopeCalculator
 * Focus on critical changes detection and test scope calculation
 */
class SerializedTestScopeCalculatorTest {

    private lateinit var tempDir: File
    private lateinit var dependencyGraph: SerializedDependencyGraph
    private lateinit var dependencyAnalyzer: SerializedDependencyAnalyzer
    private lateinit var config: TestConfig
    private lateinit var calculator: SerializedTestScopeCalculator

    @Before
    fun setUp() {
        // Create temp directory
        tempDir = File.createTempFile("test", "").apply {
            delete()
            mkdir()
        }

        // Create module directories with test files
        listOf(
            "app/src/test/kotlin",
            "features/main/impl/src/test/kotlin",
            "features/main/public_api/src/test/kotlin",
            "core/database/src/test/kotlin",
            "core/network/src/test/kotlin",
            "features/auth/src/test/kotlin"
        ).forEach { path ->
            File(tempDir, path).mkdirs()
            // Create a dummy test file
            File(tempDir, "$path/DummyTest.kt").writeText("class DummyTest")
        }

        // Setup dependency graph
        val dependencies = mapOf(
            ":app" to listOf(":features:main:impl", ":core:database", ":core:network"),
            ":features:main:impl" to listOf(":features:main:public_api", ":core:database"),
            ":features:main:public_api" to emptyList(),
            ":core:database" to emptyList(),
            ":core:network" to emptyList(),
            ":features:auth" to listOf(":core:network")
        )

        val reverseDependencies = mapOf(
            ":app" to emptyList(),
            ":features:main:impl" to listOf(":app"),
            ":features:main:public_api" to listOf(":features:main:impl", ":app"),
            ":core:database" to listOf(":app", ":features:main:impl"),
            ":core:network" to listOf(":app", ":features:auth"),
            ":features:auth" to emptyList()
        )

        val modules = setOf(
            ":app",
            ":features:main:impl",
            ":features:main:public_api",
            ":core:database",
            ":core:network",
            ":features:auth"
        )

        dependencyGraph = SerializedDependencyGraph(
            dependencies = dependencies,
            reverseDependencies = reverseDependencies,
            modules = modules
        )

        val moduleDirectories = mapOf(
            ":app" to "app",
            ":features:main:impl" to "features/main/impl",
            ":features:main:public_api" to "features/main/public_api",
            ":core:database" to "core/database",
            ":core:network" to "core/network",
            ":features:auth" to "features/auth"
        )

        dependencyAnalyzer = SerializedDependencyAnalyzer(
            rootDir = tempDir,
            moduleDirectories = moduleDirectories
        )

        // Default test configuration
        config = TestConfig(
            runAllTestsOnCriticalChanges = true,
            runUnitTestsByDefault = true,
            criticalPaths = listOf("build.gradle", "settings.gradle", "gradle.properties"),
            testTypeRules = mapOf(
                TestType.UNIT to TestTypeRule().apply {
                    isEnable = true
                    pathPatterns.add("**/*.kt")
                }
            )
        )

        val availableTestTasks = mapOf(
            ":app" to listOf("test", "testDebugUnitTest"),
            ":features:main:impl" to listOf("test", "testDebugUnitTest"),
            ":features:main:public_api" to listOf("test", "testDebugUnitTest"),
            ":core:database" to listOf("test", "testDebugUnitTest"),
            ":core:network" to listOf("test", "testDebugUnitTest"),
            ":features:auth" to listOf("test", "testDebugUnitTest")
        )

        calculator = SerializedTestScopeCalculator(
            dependencyGraph = dependencyGraph,
            dependencyAnalyzer = dependencyAnalyzer,
            config = config,
            availableTestTasks = availableTestTasks,
            androidUnitTestVariant = "Debug",
            androidInstrumentedTestVariant = "Debug"
        )
    }

    // ==================== Root-level Critical Changes Tests ====================

    @Test
    fun `calculateTestScope should run all tests for root build gradle changes`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "build.gradle.kts",
                module = null,
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        // Should run tests in ALL modules
        val unitTests = result[TestType.UNIT] ?: emptyList()
        assertTrue(unitTests.isNotEmpty(), "Should have unit tests")
        assertTrue(unitTests.contains(":app:testDebugUnitTest"), "Should include :app")
        assertTrue(unitTests.contains(":core:database:testDebugUnitTest"), "Should include :core:database")
        assertTrue(unitTests.contains(":core:network:testDebugUnitTest"), "Should include :core:network")
        assertTrue(unitTests.contains(":features:auth:testDebugUnitTest"), "Should include :features:auth")
        assertEquals(6, unitTests.size, "Should have tests for all 6 modules")
    }

    @Test
    fun `calculateTestScope should run all tests for gradle wrapper changes`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "gradle/wrapper/gradle-wrapper.properties",
                module = null,
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.PROPERTIES
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        val unitTests = result[TestType.UNIT] ?: emptyList()
        assertEquals(6, unitTests.size, "Should have tests for all modules")
    }

    @Test
    fun `calculateTestScope should run all tests for settings gradle changes`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "settings.gradle.kts",
                module = null,
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        val unitTests = result[TestType.UNIT] ?: emptyList()
        assertEquals(6, unitTests.size, "Should have tests for all modules")
    }

    // ==================== Module-level Config Changes Tests ====================

    @Test
    fun `calculateTestScope should run only affected tests for module build gradle changes`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "app/build.gradle.kts",
                module = ":app",
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        val unitTests = result[TestType.UNIT] ?: emptyList()
        // app has no reverse dependencies, so only app should be tested
        assertTrue(unitTests.contains(":app:testDebugUnitTest"), "Should include :app")
        assertFalse(
            unitTests.contains(":core:database:testDebugUnitTest"),
            "Should NOT include :core:database (not dependent on :app)"
        )
        assertFalse(
            unitTests.contains(":features:auth:testDebugUnitTest"),
            "Should NOT include :features:auth (not dependent on :app)"
        )
    }

    @Test
    fun `calculateTestScope should run affected module tests for feature build gradle changes`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "features/main/public_api/build.gradle.kts",
                module = ":features:main:public_api",
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        val unitTests = result[TestType.UNIT] ?: emptyList()
        // Modules that depend on :features:main:public_api are :features:main:impl and :app
        assertTrue(
            unitTests.contains(":features:main:public_api:testDebugUnitTest"),
            "Should include changed module"
        )
        assertTrue(
            unitTests.contains(":features:main:impl:testDebugUnitTest"),
            "Should include dependent :features:main:impl"
        )
        assertTrue(unitTests.contains(":app:testDebugUnitTest"), "Should include dependent :app")
        assertFalse(
            unitTests.contains(":core:database:testDebugUnitTest"),
            "Should NOT include unrelated :core:database"
        )
        assertFalse(
            unitTests.contains(":features:auth:testDebugUnitTest"),
            "Should NOT include unrelated :features:auth"
        )
    }

    // ==================== Regular Source Code Changes Tests ====================

    @Test
    fun `calculateTestScope should run affected tests for source code changes`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "features/main/public_api/src/main/kotlin/Api.kt",
                module = ":features:main:public_api",
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        val unitTests = result[TestType.UNIT] ?: emptyList()
        // Should include changed module and dependents
        assertTrue(unitTests.contains(":features:main:public_api:testDebugUnitTest"))
        assertTrue(unitTests.contains(":features:main:impl:testDebugUnitTest"))
        assertTrue(unitTests.contains(":app:testDebugUnitTest"))
        assertEquals(3, unitTests.size, "Should only have 3 affected modules")
    }

    // ==================== runAllTestsOnCriticalChanges = false Tests ====================

    @Test
    fun `calculateTestScope should not run all tests when runAllTestsOnCriticalChanges is false`() {
        // Update config to disable runAllTestsOnCriticalChanges
        config = config.copy(runAllTestsOnCriticalChanges = false)

        calculator = SerializedTestScopeCalculator(
            dependencyGraph = dependencyGraph,
            dependencyAnalyzer = dependencyAnalyzer,
            config = config,
            availableTestTasks = mapOf(
                ":app" to listOf("test", "testDebugUnitTest"),
                ":features:main:impl" to listOf("test", "testDebugUnitTest"),
                ":features:main:public_api" to listOf("test", "testDebugUnitTest"),
                ":core:database" to listOf("test", "testDebugUnitTest"),
                ":core:network" to listOf("test", "testDebugUnitTest"),
                ":features:auth" to listOf("test", "testDebugUnitTest")
            ),
            androidUnitTestVariant = "Debug",
            androidInstrumentedTestVariant = "Debug"
        )

        val changedFiles = listOf(
            ChangedFile(
                path = "build.gradle.kts",
                module = null,
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        // Should be empty or run only unit tests by default
        val unitTests = result[TestType.UNIT] ?: emptyList()
        // When no module is affected and runUnitTestsByDefault is true, it runs unit tests for affected modules
        // Since build.gradle.kts has no module, there should be no tests
        assertTrue(unitTests.isEmpty(), "Should not run all tests when flag is disabled")
    }

    // ==================== Multiple File Changes Tests ====================

    @Test
    fun `calculateTestScope should run all tests when both root and module files change`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "build.gradle.kts",
                module = null,
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            ),
            ChangedFile(
                path = "app/build.gradle.kts",
                module = ":app",
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        val unitTests = result[TestType.UNIT] ?: emptyList()
        // Root file triggers all tests
        assertEquals(6, unitTests.size, "Should have tests for all modules due to root file change")
    }

    @Test
    fun `calculateTestScope should run affected tests for multiple module changes`() {
        val changedFiles = listOf(
            ChangedFile(
                path = "core/database/src/main/kotlin/Db.kt",
                module = ":core:database",
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            ),
            ChangedFile(
                path = "core/network/src/main/kotlin/Api.kt",
                module = ":core:network",
                changeType = ChangeType.MODIFIED,
                language = FileLanguage.KOTLIN
            )
        )

        val result = calculator.calculateTestScope(changedFiles)

        val unitTests = result[TestType.UNIT] ?: emptyList()
        // :core:database affects: :core:database, :features:main:impl, :app
        // :core:network affects: :core:network, :app, :features:auth
        // Combined: all modules that depend on either
        assertTrue(unitTests.contains(":core:database:testDebugUnitTest"))
        assertTrue(unitTests.contains(":core:network:testDebugUnitTest"))
        assertTrue(unitTests.contains(":app:testDebugUnitTest"))
        assertTrue(unitTests.contains(":features:main:impl:testDebugUnitTest"))
        assertTrue(unitTests.contains(":features:auth:testDebugUnitTest"))
    }

    // ==================== Helper Data Class ====================

    private data class TestConfig(
        override val runAllTestsOnCriticalChanges: Boolean,
        override val runUnitTestsByDefault: Boolean,
        override val criticalPaths: List<String>,
        override val testTypeRules: Map<TestType, TestTypeRule>,
        override val lintFileExtensions: List<String> = listOf("kt", "java")
    ) : ImpactAnalysisConfig
}
