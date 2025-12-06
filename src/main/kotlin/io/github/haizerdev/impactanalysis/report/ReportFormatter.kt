package io.github.haizerdev.impactanalysis.report

import io.github.haizerdev.impactanalysis.model.ImpactReport

/**
 * Formatter for Impact Analysis Report
 * Provides unified formatting for console output, bash scripts, and other outputs
 */
object ReportFormatter {

    /**
     * Format report as a beautiful box for console/bash output
     */
    fun formatAsBox(report: ImpactReport, includeDetails: Boolean = false): String {
        return buildString {
            appendLine("╔════════════════════════════════════════════════════════╗")
            appendLine("║  IMPACT ANALYSIS REPORT")
            appendLine("╠════════════════════════════════════════════════════════╣")
            appendLine("║")
            appendLine("║  📊 Test Tasks:")
            appendLine("║     • Tasks to run:      ${report.totalTestsToRun}")
            appendLine("║     • Tasks skipped:     ${report.totalTestsSkipped}")
            appendLine("║")

            if (report.totalTestMethodsToRun > 0 || report.totalTestMethodsSkipped > 0) {
                appendLine("║  🧪 Test Methods:")
                appendLine("║     • Methods to run:    ${report.totalTestMethodsToRun}")
                appendLine("║     • Methods skipped:   ${report.totalTestMethodsSkipped}")
                appendLine("║")
            }

            appendLine("║  📦 Modules:")
            appendLine("║     • Modules to run:    ${report.modulesToRun.size}")
            appendLine("║     • Modules skipped:   ${report.skippedModules.size}")
            appendLine("║")
            appendLine("║  ⏱️  Time Estimation:")
            appendLine("║     • Estimated time saved: %.1f minutes".format(report.estimatedTimeSavedMinutes))

            if (includeDetails) {
                appendLine("║")
                appendLine("╠════════════════════════════════════════════════════════╣")

                // Tests by type
                if (report.testsByType.isNotEmpty()) {
                    appendLine("║")
                    appendLine("║  🔍 Tests by Type:")
                    report.testsByType.forEach { (type, count) ->
                        appendLine("║     • $type: $count")
                    }
                }

                // Modules to run
                if (report.modulesToRun.isNotEmpty()) {
                    appendLine("║  📦 Modules to run:")
                    val sortedModules = report.modulesToRun.sorted().take(500)
                    sortedModules.chunked(3).forEach { chunk ->
                        appendLine("║     ${chunk.joinToString(", ")}")
                    }
                    if (report.modulesToRun.size > 500) {
                        appendLine("║     ... and ${report.modulesToRun.size - 500} more")
                    }
                    appendLine("║")
                }

                // Skipped modules
                if (report.skippedModules.isNotEmpty()) {
                    appendLine("║")
                    appendLine("║  ⏭️  Skipped Modules:")
                    val sortedSkippedModules = report.skippedModules.sorted().take(500)
                    sortedSkippedModules.chunked(3).forEach { chunk ->
                        appendLine("║     ${chunk.joinToString(", ")}")
                    }
                    if (report.skippedModules.size > 500) {
                        appendLine("║     ... and ${report.skippedModules.size - 500} more")
                    }
                    appendLine("║")
                }

                // Test methods by module (top 10)
                if (report.testMethodsByModule.isNotEmpty()) {
                    appendLine("║")
                    appendLine("║  📋 Test Methods by Module (Top 10):")
                    report.testMethodsByModule
                        .toList()
                        .sortedByDescending { it.second }
                        .take(10)
                        .forEach { (module, count) ->
                            val status = if (module in report.modulesToRun) "▶️" else "⏸️"
                            appendLine("║     $status $module: $count")
                        }
                }
            }

            appendLine("║")
            appendLine("╚════════════════════════════════════════════════════════╝")
        }
    }

    /**
     * Format report for bash script (with echo commands)
     */
    fun formatForBashScript(report: ImpactReport): String {
        return buildString {
            appendLine("echo \"\"")
            formatAsBox(report, includeDetails = false).lines().forEach { line ->
                if (line.isNotEmpty()) {
                    appendLine("echo \"$line\"")
                }
            }
            appendLine("echo \"\"")
            appendLine("echo \"Starting tests...\"")
            appendLine("echo \"\"")
        }
    }

    /**
     * Format simple summary for console log
     */
    fun formatSummary(report: ImpactReport): List<String> {
        return listOf(
            "========== Impact Analysis Report ==========",
            "Total tests to run: ${report.totalTestsToRun}",
            "Total tests skipped: ${report.totalTestsSkipped}",
            "Modules to run (${report.modulesToRun.size}): ${report.modulesToRun.joinToString(", ")}",
            "Skipped modules (${report.skippedModules.size}): ${report.skippedModules.joinToString(", ")}",
            "Estimated time saved: %.1f minutes".format(report.estimatedTimeSavedMinutes),
            "Total test methods to run: ${report.totalTestMethodsToRun}",
            "Total test methods skipped: ${report.totalTestMethodsSkipped}",
            "==========================================="
        )
    }
}
