package io.github.haizerdev.impactanalysis.tasks

import io.github.haizerdev.impactanalysis.git.GitClient
import io.github.haizerdev.impactanalysis.git.GitDiffEntry
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Task for getting list of changed files
 * Configuration cache compatible
 */
abstract class GetChangedFilesTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val baseBranch: Property<String>

    @get:Input
    @get:Optional
    abstract val compareBranch: Property<String>

    @get:Input
    abstract val includeUncommittedChanges: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val fileExtensions: ListProperty<String>

    @get:Internal
    abstract val rootProjectDir: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = "impact analysis"
        description = "Get list of changed files from Git"

        outputFile.convention(
            project.layout.buildDirectory.file("impact-analysis/changed-files.txt")
        )

        // Disable up-to-date check, as task depends on Git state
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute() {
        val rootDir = rootProjectDir.get().asFile
        val gitClient = GitClient(rootDir)

        try {
            // Get changes
            val changes = mutableListOf<GitDiffEntry>()

            if (includeUncommittedChanges.get()) {
                changes.addAll(gitClient.getUncommittedChanges())
            }

            val base = baseBranch.get()
            val compare = compareBranch.get()

            try {
                changes.addAll(gitClient.getChangedFiles(base, compare))
            } catch (e: Exception) {
                logger.warn("Failed to get changes: ${e.message}")
            }

            // Filter by extensions if specified
            val extensions = fileExtensions.orNull
            val filteredFiles = if (extensions != null && extensions.isNotEmpty()) {
                changes.filter { change ->
                    val fileExtension = change.newPath.substringAfterLast('.', "")
                    fileExtension in extensions
                }
            } else {
                changes
            }.map { it.newPath }

            logger.lifecycle("Found ${filteredFiles.size} changed files")

            // Save result
            val file = outputFile.get().asFile
            file.parentFile.mkdirs()

            if (filteredFiles.isEmpty()) {
                file.writeText("")
                logger.lifecycle("No changed files found")
            } else {
                file.writeText(filteredFiles.joinToString("\n"))
                logger.lifecycle("Changed files saved to: $file")

                filteredFiles.forEach { filePath ->
                    logger.lifecycle("  - $filePath")
                }
            }

        } finally {
            gitClient.close()
        }
    }
}
