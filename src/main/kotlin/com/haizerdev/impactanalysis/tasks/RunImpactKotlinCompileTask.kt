package com.haizerdev.impactanalysis.tasks

import com.google.gson.Gson
import com.haizerdev.impactanalysis.model.ImpactAnalysisResult
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Task for running Kotlin compilation based on impact analysis results
 */
abstract class RunImpactKotlinCompileTask : DefaultTask() {

    @get:InputFile
    abstract val impactResultFile: RegularFileProperty

    @get:Input
    abstract val continueOnFailure: Property<Boolean>

    init {
        group = "impact analysis"
        description = "Run Kotlin compilation based on impact analysis results"

        impactResultFile.convention(
            project.layout.buildDirectory.file("impact-analysis/result.json")
        )

        continueOnFailure.convention(false)
    }

    @TaskAction
    fun execute() {
        val resultFile = impactResultFile.get().asFile

        if (!resultFile.exists()) {
            logger.error("Impact analysis result file not found: $resultFile")
            logger.error("Run 'calculateImpact' task first")
            throw IllegalStateException("Impact analysis result not found")
        }

        // Read result
        val gson = Gson()
        val result = gson.fromJson(resultFile.readText(), ImpactAnalysisResult::class.java)

        if (result.affectedModules.isEmpty()) {
            logger.lifecycle("No modules affected - skipping compilation")
            return
        }

        logger.lifecycle("=".repeat(60))
        logger.lifecycle("Running Kotlin Compilation for Affected Modules")
        logger.lifecycle("=".repeat(60))

        val failedTasks = mutableListOf<String>()
        var totalTasks = 0
        var successfulTasks = 0

        // Compile affected modules
        result.affectedModules.forEach { modulePath ->
            val compileTaskPath = if (modulePath == ":") {
                ":compileKotlin"
            } else {
                "$modulePath:compileKotlin"
            }

            totalTasks++
            logger.lifecycle("\nCompiling module: $modulePath")
            logger.lifecycle("Task: $compileTaskPath")
            logger.lifecycle("-".repeat(60))

            try {
                // Try running through exec
                val execResult = project.exec { spec ->
                    spec.workingDir = project.rootProject.projectDir
                    if (System.getProperty("os.name").lowercase().contains("win")) {
                        spec.commandLine("cmd", "/c", "gradlew.bat", compileTaskPath)
                    } else {
                        spec.commandLine("./gradlew", compileTaskPath)
                    }
                    spec.isIgnoreExitValue = continueOnFailure.get()
                }

                if (execResult.exitValue == 0) {
                    successfulTasks++
                    logger.lifecycle("✓ $compileTaskPath completed successfully")
                } else {
                    failedTasks.add(compileTaskPath)
                    logger.error("✗ $compileTaskPath failed")

                    if (!continueOnFailure.get()) {
                        throw RuntimeException("Compilation task $compileTaskPath failed")
                    }
                }
            } catch (e: Exception) {
                failedTasks.add(compileTaskPath)
                logger.error("✗ $compileTaskPath failed: ${e.message}")

                if (!continueOnFailure.get()) {
                    throw e
                }
            }
        }

        // Final report
        logger.lifecycle("\n" + "=".repeat(60))
        logger.lifecycle("Kotlin Compilation Summary")
        logger.lifecycle("=".repeat(60))
        logger.lifecycle("Total tasks: $totalTasks")
        logger.lifecycle("Successful: $successfulTasks")
        logger.lifecycle("Failed: ${failedTasks.size}")

        if (failedTasks.isNotEmpty()) {
            logger.lifecycle("\nFailed tasks:")
            failedTasks.forEach { task ->
                logger.lifecycle("  ✗ $task")
            }

            if (!continueOnFailure.get()) {
                throw RuntimeException("${failedTasks.size} compilation task(s) failed")
            }
        }

        logger.lifecycle("=".repeat(60))
    }
}
