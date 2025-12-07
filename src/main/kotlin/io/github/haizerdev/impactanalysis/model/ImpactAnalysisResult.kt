package io.github.haizerdev.impactanalysis.model

import java.io.Serializable

/**
 * Impact analysis result
 */
data class ImpactAnalysisResult(
    val changedFiles: List<ChangedFile>,
    val affectedModules: Set<String>,
    val testsToRun: Map<TestType, List<String>>,
    val filesToLint: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val report: ImpactReport? = null
) : Serializable

/**
 * Changed file information
 */
data class ChangedFile(
    val path: String,
    val module: String?,
    val changeType: ChangeType,
    val language: FileLanguage?
) : Serializable

/**
 * Git change type
 */
enum class ChangeType {
    ADDED,
    MODIFIED,
    DELETED,
    RENAMED,
    COPIED
}

/**
 * File programming language
 */
enum class FileLanguage(val extensions: List<String>) {
    KOTLIN(listOf("kt", "kts")),
    JAVA(listOf("java")),
    XML(listOf("xml")),
    JSON(listOf("json")),
    GROOVY(listOf("groovy", "gradle")),
    PROPERTIES(listOf("properties")),
    YAML(listOf("yaml", "yml")),
    UNKNOWN(emptyList());

    companion object {
        fun fromExtension(extension: String): FileLanguage {
            return values().find {
                it.extensions.any { ext -> ext.equals(extension, ignoreCase = true) }
            } ?: UNKNOWN
        }

        fun fromPath(path: String): FileLanguage {
            val extension = path.substringAfterLast('.', "")
            return fromExtension(extension)
        }
    }
}

/**
 * Impact analysis statistics and report
 */
data class ImpactReport(
    val totalTestsToRun: Int,
    val totalTestsSkipped: Int,
    val modulesToRun: Set<String>,
    val skippedModules: Set<String>,
    val estimatedTimeSavedMinutes: Double,
    val testsByType: Map<TestType, Int>,
    val totalTestMethodsToRun: Int = 0,
    val totalTestMethodsSkipped: Int = 0,
    val testMethodsByModule: Map<String, Int> = emptyMap()
) : Serializable

/**
 * Format time in a human-readable way
 * - Less than 60 seconds: "45 sec"
 * - 60+ seconds: "2 min 30 sec"
 */
fun formatEstimatedTime(minutes: Double): String {
    val totalSeconds = (minutes * 60).toInt()

    return when {
        totalSeconds < 60 -> "$totalSeconds sec"
        else -> {
            val mins = totalSeconds / 60
            val secs = totalSeconds % 60
            if (secs > 0) {
                "$mins min $secs sec"
            } else {
                "$mins min"
            }
        }
    }
}
