// Пример конфигурации для простого multi-module проекта

plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}

impactAnalysis {
    // Сравниваем с main веткой
    baseBranch.set("origin/main")

    // Включаем анализ uncommitted изменений
    includeUncommittedChanges.set(true)

    // Unit тесты запускаются для всех измененных модулей и их зависимых
    unitTests {
        whenChanged("src/main/**", "src/test/**")
        runOnlyInChangedModules = false
    }

    // Файлы для линтинга
    lintFileExtensions.set(listOf("kt", "java"))
}

// Интеграция с detekt
tasks.register("detektChangedFiles") {
    group = "verification"
    description = "Run detekt only on changed files"

    dependsOn("getChangedFilesForLint")

    doLast {
        val lintFiles = file("build/impact-analysis/lint-files.txt")
        if (lintFiles.exists() && lintFiles.readText().isNotBlank()) {
            exec {
                commandLine(
                    "./gradlew", "detekt",
                    "-Pdetekt.baseline=detekt-baseline.xml"
                )
            }
        } else {
            println("No files to lint")
        }
    }
}
