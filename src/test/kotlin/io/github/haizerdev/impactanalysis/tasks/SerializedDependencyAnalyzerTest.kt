package io.github.haizerdev.impactanalysis.tasks

import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for SerializedDependencyAnalyzer
 * Focus on critical file detection logic
 */
class SerializedDependencyAnalyzerTest {

    private lateinit var tempDir: File
    private lateinit var analyzer: SerializedDependencyAnalyzer

    @Before
    fun setUp() {
        // Create temp directory
        tempDir = File.createTempFile("test", "").apply {
            delete()
            mkdir()
        }

        val moduleDirectories = mapOf(
            ":app" to "app",
            ":features:main:impl" to "features/main/impl",
            ":features:main:public_api" to "features/main/public_api",
            ":core:database" to "core/database",
            ":core:network" to "core/network"
        )

        analyzer = SerializedDependencyAnalyzer(
            rootDir = tempDir,
            moduleDirectories = moduleDirectories
        )
    }

    // ==================== isConfigFile() Tests ====================

    @Test
    fun `isConfigFile should return true for build gradle files`() {
        assertTrue(analyzer.isConfigFile("build.gradle"))
        assertTrue(analyzer.isConfigFile("build.gradle.kts"))
        assertTrue(analyzer.isConfigFile("app/build.gradle"))
        assertTrue(analyzer.isConfigFile("app/build.gradle.kts"))
        assertTrue(analyzer.isConfigFile("features/main/impl/build.gradle.kts"))
    }

    @Test
    fun `isConfigFile should return true for settings gradle files`() {
        assertTrue(analyzer.isConfigFile("settings.gradle"))
        assertTrue(analyzer.isConfigFile("settings.gradle.kts"))
    }

    @Test
    fun `isConfigFile should return true for gradle properties files`() {
        assertTrue(analyzer.isConfigFile("gradle.properties"))
        assertTrue(analyzer.isConfigFile("app/gradle.properties"))
        assertTrue(analyzer.isConfigFile("local.properties"))
    }

    @Test
    fun `isConfigFile should return true for gradle directory files`() {
        assertTrue(analyzer.isConfigFile("gradle/wrapper/gradle-wrapper.properties"))
        assertTrue(analyzer.isConfigFile("gradle/wrapper/gradle-wrapper.jar"))
        assertTrue(analyzer.isConfigFile("gradle/libs.versions.toml"))
    }

    @Test
    fun `isConfigFile should return true for ProGuard files`() {
        assertTrue(analyzer.isConfigFile("proguard-rules.pro"))
        assertTrue(analyzer.isConfigFile("app/proguard-rules.pro"))
    }

    @Test
    fun `isConfigFile should return false for source files`() {
        assertFalse(analyzer.isConfigFile("app/src/main/kotlin/MainActivity.kt"))
        assertFalse(analyzer.isConfigFile("features/main/impl/src/main/java/Feature.java"))
        assertFalse(analyzer.isConfigFile("README.md"))
    }

    // ==================== isRootLevelConfigFile() Tests ====================

    @Test
    fun `isRootLevelConfigFile should return true for root build gradle files`() {
        assertTrue(analyzer.isRootLevelConfigFile("build.gradle"))
        assertTrue(analyzer.isRootLevelConfigFile("build.gradle.kts"))
    }

    @Test
    fun `isRootLevelConfigFile should return false for module build gradle files`() {
        assertFalse(analyzer.isRootLevelConfigFile("app/build.gradle"))
        assertFalse(analyzer.isRootLevelConfigFile("app/build.gradle.kts"))
        assertFalse(analyzer.isRootLevelConfigFile("features/main/impl/build.gradle.kts"))
        assertFalse(analyzer.isRootLevelConfigFile("core/database/build.gradle"))
    }

    @Test
    fun `isRootLevelConfigFile should return true for settings gradle files`() {
        // settings.gradle is always root-level
        assertTrue(analyzer.isRootLevelConfigFile("settings.gradle"))
        assertTrue(analyzer.isRootLevelConfigFile("settings.gradle.kts"))
    }

    @Test
    fun `isRootLevelConfigFile should return true for root gradle properties`() {
        assertTrue(analyzer.isRootLevelConfigFile("gradle.properties"))
    }

    @Test
    fun `isRootLevelConfigFile should return false for module gradle properties`() {
        assertFalse(analyzer.isRootLevelConfigFile("app/gradle.properties"))
        assertFalse(analyzer.isRootLevelConfigFile("features/main/gradle.properties"))
    }

    @Test
    fun `isRootLevelConfigFile should return true for gradle directory files`() {
        assertTrue(analyzer.isRootLevelConfigFile("gradle/wrapper/gradle-wrapper.properties"))
        assertTrue(analyzer.isRootLevelConfigFile("gradle/wrapper/gradle-wrapper.jar"))
        assertTrue(analyzer.isRootLevelConfigFile("gradle/libs.versions.toml"))
        assertTrue(analyzer.isRootLevelConfigFile("gradle/something.xml"))
    }

    @Test
    fun `isRootLevelConfigFile should return false for non-config files`() {
        assertFalse(analyzer.isRootLevelConfigFile("README.md"))
        assertFalse(analyzer.isRootLevelConfigFile("app/src/main/kotlin/MainActivity.kt"))
        assertFalse(analyzer.isRootLevelConfigFile("features/Feature.kt"))
    }

    @Test
    fun `isRootLevelConfigFile should return false for ProGuard files in modules`() {
        // ProGuard files are config files but not root-level
        assertFalse(analyzer.isRootLevelConfigFile("app/proguard-rules.pro"))
        assertFalse(analyzer.isRootLevelConfigFile("features/proguard-rules.pro"))
    }

    @Test
    fun `isRootLevelConfigFile should handle Windows paths`() {
        assertTrue(analyzer.isRootLevelConfigFile("gradle\\wrapper\\gradle-wrapper.properties"))
        assertFalse(analyzer.isRootLevelConfigFile("app\\build.gradle.kts"))
        assertTrue(analyzer.isRootLevelConfigFile("build.gradle.kts"))
    }

    @Test
    fun `isRootLevelConfigFile should handle mixed path separators`() {
        assertTrue(analyzer.isRootLevelConfigFile("gradle/wrapper\\gradle-wrapper.properties"))
        assertFalse(analyzer.isRootLevelConfigFile("app\\features/build.gradle.kts"))
    }

    // ==================== isTestFile() Tests ====================

    @Test
    fun `isTestFile should return true for test directory files`() {
        assertTrue(analyzer.isTestFile("app/src/test/kotlin/AppTest.kt"))
        assertTrue(analyzer.isTestFile("app/src/test/java/AppTest.java"))
        assertTrue(analyzer.isTestFile("features/main/impl/src/test/kotlin/FeatureTest.kt"))
    }

    @Test
    fun `isTestFile should return true for androidTest directory files`() {
        assertTrue(analyzer.isTestFile("app/src/androidTest/kotlin/AppInstrumentedTest.kt"))
        assertTrue(analyzer.isTestFile("app/src/androidTestDebug/kotlin/DebugTest.kt"))
        assertTrue(analyzer.isTestFile("app/src/androidTestRelease/kotlin/ReleaseTest.kt"))
    }

    @Test
    fun `isTestFile should return true for files ending with Test`() {
        assertTrue(analyzer.isTestFile("app/src/main/kotlin/SomeTest.kt"))
        assertTrue(analyzer.isTestFile("features/MyTest.java"))
        assertTrue(analyzer.isTestFile("core/UtilsTests.kt"))
        assertTrue(analyzer.isTestFile("api/ServiceSpec.kt"))
    }

    @Test
    fun `isTestFile should return false for production files`() {
        assertFalse(analyzer.isTestFile("app/src/main/kotlin/MainActivity.kt"))
        assertFalse(analyzer.isTestFile("features/Feature.kt"))
        assertFalse(analyzer.isTestFile("core/Utils.java"))
    }

    // ==================== getModuleForFile() Tests ====================

    @Test
    fun `getModuleForFile should return correct module for app files`() {
        assertEquals(":app", analyzer.getModuleForFile("app/src/main/kotlin/MainActivity.kt"))
        assertEquals(":app", analyzer.getModuleForFile("app/build.gradle.kts"))
    }

    @Test
    fun `getModuleForFile should return correct module for nested feature files`() {
        assertEquals(
            ":features:main:impl",
            analyzer.getModuleForFile("features/main/impl/src/main/kotlin/Feature.kt")
        )
        assertEquals(
            ":features:main:public_api",
            analyzer.getModuleForFile("features/main/public_api/src/main/kotlin/Api.kt")
        )
    }

    @Test
    fun `getModuleForFile should return correct module for core files`() {
        assertEquals(":core:database", analyzer.getModuleForFile("core/database/src/main/kotlin/Db.kt"))
        assertEquals(":core:network", analyzer.getModuleForFile("core/network/src/main/kotlin/Api.kt"))
    }

    @Test
    fun `getModuleForFile should return null for root-level files`() {
        assertNull(analyzer.getModuleForFile("build.gradle.kts"))
        assertNull(analyzer.getModuleForFile("settings.gradle.kts"))
        assertNull(analyzer.getModuleForFile("gradle.properties"))
        assertNull(analyzer.getModuleForFile("README.md"))
    }

    @Test
    fun `getModuleForFile should handle Windows paths`() {
        assertEquals(":app", analyzer.getModuleForFile("app\\src\\main\\kotlin\\MainActivity.kt"))
        assertEquals(
            ":features:main:impl",
            analyzer.getModuleForFile("features\\main\\impl\\src\\main\\kotlin\\Feature.kt")
        )
    }

    @Test
    fun `getModuleForFile should cache results`() {
        // First call
        val module1 = analyzer.getModuleForFile("app/src/main/kotlin/MainActivity.kt")
        // Second call should use cache
        val module2 = analyzer.getModuleForFile("app/src/main/kotlin/MainActivity.kt")

        assertEquals(module1, module2)
        assertEquals(":app", module2)
    }

    @Test
    fun `getModuleForFile should match longest path for nested modules`() {
        // If we have both :features and :features:main:impl
        // "features/main/impl/File.kt" should match :features:main:impl, not :features
        val moduleDirectoriesWithNesting = mapOf(
            ":features" to "features",
            ":features:main" to "features/main",
            ":features:main:impl" to "features/main/impl"
        )

        val analyzerWithNesting = SerializedDependencyAnalyzer(
            rootDir = tempDir,
            moduleDirectories = moduleDirectoriesWithNesting
        )

        assertEquals(
            ":features:main:impl",
            analyzerWithNesting.getModuleForFile("features/main/impl/src/main/kotlin/File.kt")
        )
    }

    // ==================== Edge Cases ====================

    @Test
    fun `isRootLevelConfigFile should handle empty path`() {
        assertFalse(analyzer.isRootLevelConfigFile(""))
    }

    @Test
    fun `isConfigFile should handle empty path`() {
        assertFalse(analyzer.isConfigFile(""))
    }

    @Test
    fun `isTestFile should handle empty path`() {
        assertFalse(analyzer.isTestFile(""))
    }

    @Test
    fun `getModuleForFile should handle empty path`() {
        assertNull(analyzer.getModuleForFile(""))
    }
}
