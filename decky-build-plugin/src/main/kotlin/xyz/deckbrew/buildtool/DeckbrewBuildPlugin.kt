package xyz.deckbrew.buildtool

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import xyz.deckbrew.buildtool.backend.gradle.GradlePipelineProvider
import xyz.deckbrew.buildtool.backend.rollup.RollupPipelineProvider
import java.io.File

class DeckbrewBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.createTasks()
    }

    /**
     * List of supported build pipelines
     */
    private val providers = listOf(
        ::GradlePipelineProvider,
        ::RollupPipelineProvider,
    )

    private fun Project.createTasks() {
        tasks {
            // Single task which runs all the build pipelines
            val allBuild by registering {
                group = "deckbrew"
            }

            // Find all changed plugins, or if none are changed, all plugins
            val plugins = System.getenv("CHANGED_FILES")?.let {
                it.split(",").map(::File).filter(File::isDirectory).ifEmpty { null }
            } ?: project.file("plugins").listFiles(File::isDirectory)?.toList() ?: emptyList()

            plugins@for (plugin in plugins) {
                for (provider in this@DeckbrewBuildPlugin.providers) {
                    val handler = provider(project, plugin, buildDir.resolve("plugins/${plugin.name}"))

                    // If the plugin is not supported by this pipeline, skip it
                    if (handler.matches()) {
                        handler.create()
                        continue@plugins
                    }
                }

                throw IllegalStateException("No provider found for plugin ${plugin.name}")
            }
        }
    }
}
