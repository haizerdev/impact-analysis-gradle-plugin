package io.github.haizerdev.impactanalysis.model

import com.google.gson.Gson
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for ImpactAnalysisResult and ImpactReport
 */
class ImpactAnalysisResultTest {

    @Test
    fun `test ImpactReport serialization and deserialization`() {
        val report = ImpactReport(
            totalTestsToRun = 5,
            totalTestsSkipped = 15,
            modulesToRun = setOf(":app", ":features:auth"),
            skippedModules = setOf(":core:database", ":core:network", ":features:profile"),
            estimatedTimeSavedMinutes = 7.5,
            testsByType = mapOf(
                TestType.UNIT to 3,
                TestType.UI to 2
            ),
            totalTestMethodsToRun = 150,
            totalTestMethodsSkipped = 800,
            testMethodsByModule = mapOf(
                ":app" to 80,
                ":features:auth" to 70,
                ":core:database" to 300,
                ":core:network" to 250,
                ":features:profile" to 250
            )
        )

        val result = ImpactAnalysisResult(
            changedFiles = listOf(
                ChangedFile(
                    path = "app/src/main/kotlin/Main.kt",
                    module = ":app",
                    changeType = ChangeType.MODIFIED,
                    language = FileLanguage.KOTLIN
                )
            ),
            affectedModules = setOf(":app"),
            testsToRun = mapOf(TestType.UNIT to listOf(":app:test")),
            filesToLint = listOf("app/src/main/kotlin/Main.kt"),
            report = report
        )

        // Serialize to JSON
        val gson = Gson()
        val json = gson.toJson(result)

        // Deserialize from JSON
        val deserialized = gson.fromJson(json, ImpactAnalysisResult::class.java)

        // Verify
        assertNotNull(deserialized.report)
        assertEquals(5, deserialized.report?.totalTestsToRun)
        assertEquals(15, deserialized.report?.totalTestsSkipped)
        assertEquals(2, deserialized.report?.modulesToRun?.size)
        assertEquals(3, deserialized.report?.skippedModules?.size)
        assertEquals(7.5, deserialized.report?.estimatedTimeSavedMinutes)
        assertEquals(2, deserialized.report?.testsByType?.size)
        assertEquals(150, deserialized.report?.totalTestMethodsToRun)
        assertEquals(800, deserialized.report?.totalTestMethodsSkipped)
        assertEquals(5, deserialized.report?.testMethodsByModule?.size)
    }

    @Test
    fun `test ImpactReport with empty data`() {
        val report = ImpactReport(
            totalTestsToRun = 0,
            totalTestsSkipped = 0,
            modulesToRun = emptySet(),
            skippedModules = emptySet(),
            estimatedTimeSavedMinutes = 0.0,
            testsByType = emptyMap(),
            totalTestMethodsToRun = 0,
            totalTestMethodsSkipped = 0,
            testMethodsByModule = emptyMap()
        )

        assertEquals(0, report.totalTestsToRun)
        assertEquals(0, report.totalTestsSkipped)
        assertEquals(0, report.modulesToRun.size)
        assertEquals(0, report.skippedModules.size)
        assertEquals(0.0, report.estimatedTimeSavedMinutes)
        assertEquals(0, report.testsByType.size)
        assertEquals(0, report.totalTestMethodsToRun)
        assertEquals(0, report.totalTestMethodsSkipped)
        assertEquals(0, report.testMethodsByModule.size)
    }

    @Test
    fun `test formatEstimatedTime function`() {
        // Test seconds (< 60 seconds)
        assertEquals("0 sec", formatEstimatedTime(0.0))
        assertEquals("10 sec", formatEstimatedTime(10.0 / 60.0))
        assertEquals("45 sec", formatEstimatedTime(45.0 / 60.0))
        assertEquals("59 sec", formatEstimatedTime(59.0 / 60.0))

        // Test minutes (>= 60 seconds)
        assertEquals("1 min", formatEstimatedTime(1.0))
        assertEquals("2 min 30 sec", formatEstimatedTime(2.5))
        assertEquals("5 min", formatEstimatedTime(5.0))
        assertEquals("10 min 15 sec", formatEstimatedTime(10.25))
        assertEquals("60 min", formatEstimatedTime(60.0))
    }

    @Test
    fun `test ImpactAnalysisResult without report`() {
        val result = ImpactAnalysisResult(
            changedFiles = emptyList(),
            affectedModules = emptySet(),
            testsToRun = emptyMap(),
            filesToLint = emptyList(),
            report = null
        )

        assertEquals(null, result.report)
    }
}
