package com.nzr.impactanalysis.tasks

import com.google.gson.Gson
import com.nzr.impactanalysis.model.ImpactAnalysisResult
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Задача для запуска тестов на основе результатов impact analysis
 */
abstract class RunImpactTestsTask : DefaultTask() {

    @get:InputFile
    abstract val impactResultFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val testTypes: Property<String>

    @get:Input
    abstract val continueOnFailure: Property<Boolean>

    init {
        group = "impact analysis"
        description = "Run tests based on impact analysis results"

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

        // Читаем результат
        val gson = Gson()
        val result = gson.fromJson(resultFile.readText(), ImpactAnalysisResult::class.java)

        if (result.testsToRun.isEmpty()) {
            logger.lifecycle("No tests to run")
            return
        }

        // Фильтруем по типам тестов если указаны
        val requestedTypes = testTypes.orNull?.split(",")?.map { it.trim() }
        val testsToRun = if (requestedTypes != null) {
            result.testsToRun.filterKeys { type ->
                requestedTypes.any { it.equals(type.name, ignoreCase = true) }
            }
        } else {
            result.testsToRun
        }

        logger.lifecycle("=".repeat(60))
        logger.lifecycle("Running Impact Tests")
        logger.lifecycle("=".repeat(60))

        val failedTasks = mutableListOf<String>()
        var totalTasks = 0
        var successfulTasks = 0

        testsToRun.forEach { (testType, tasks) ->
            logger.lifecycle("\n${testType.name} Tests (${tasks.size} tasks):")
            logger.lifecycle("-".repeat(60))

            if (tasks.isEmpty()) {
                return@forEach
            }

            totalTasks += tasks.size

            // Выводим список задач, которые будут запущены
            tasks.forEach { taskPath ->
                logger.lifecycle("  - $taskPath")
            }

            try {
                // Запускаем все задачи одной командой
                val gradleCommand = if (System.getProperty("os.name").lowercase().contains("win")) {
                    listOf("cmd", "/c", "gradlew.bat") + tasks + listOf("--console=plain")
                } else {
                    listOf("./gradlew") + tasks + listOf("--console=plain")
                }

                logger.lifecycle("\nExecuting: ${tasks.joinToString(" ")}")

                val execResult = project.exec { spec ->
                    spec.workingDir = project.rootProject.projectDir
                    spec.commandLine = gradleCommand
                    spec.isIgnoreExitValue = continueOnFailure.get()
                }

                if (execResult.exitValue == 0) {
                    successfulTasks += tasks.size
                    logger.lifecycle("✓ All ${testType.name} tests completed successfully")
                } else {
                    failedTasks.addAll(tasks)
                    logger.error("✗ ${testType.name} tests failed with exit code: ${execResult.exitValue}")

                    if (!continueOnFailure.get()) {
                        throw RuntimeException("${testType.name} tests failed with exit code: ${execResult.exitValue}")
                    }
                }
            } catch (e: Exception) {
                failedTasks.addAll(tasks)
                logger.error("✗ ${testType.name} tests failed: ${e.message}")

                if (!continueOnFailure.get()) {
                    throw e
                }
            }
        }

        // Итоговый отчет
        logger.lifecycle("\n" + "=".repeat(60))
        logger.lifecycle("Impact Tests Summary")
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
                throw RuntimeException("${failedTasks.size} test task(s) failed")
            }
        }

        logger.lifecycle("=".repeat(60))
    }
}
