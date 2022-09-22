package xyz.deckbrew.buildtool.backend.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.*
import xyz.deckbrew.buildtool.PluginMetadata
import xyz.deckbrew.buildtool.backend.common.BuildPipelineProvider
import xyz.deckbrew.buildtool.tasks.gradle.GradleRunTask
import java.io.File

class GradlePipelineProvider(project: Project, pluginFolder: File, buildDir: File) :
    BuildPipelineProvider(project, pluginFolder, buildDir) {

    override fun matches() = pluginHasFile("gradlew")

    override fun metadata(): PluginMetadata {
        project.exec {
            workingDir = pluginFolder
            commandLine("./gradlew", "prepareMetadata")
        }

        return metadataFromJson()
    }

    override fun TaskContainerScope.createTasks(): Task {
        val build by register<GradleRunTask>(taskName("buildGradle")) {
            group = "deckbrew"

            runDirectory.set(pluginFolder)
            task.set(listOf("build"))

            outputs.dir(pluginFolder.resolve("build/bundled"))
        }

        val copy by register<Copy>(taskName("copy")) {
            group = "deckbrew"

            destinationDir = buildDir.resolve("bundled")

            from(build)
        }

        return copy
    }
}
