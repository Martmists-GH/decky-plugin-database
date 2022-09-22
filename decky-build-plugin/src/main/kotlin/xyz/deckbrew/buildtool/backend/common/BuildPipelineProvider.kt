package xyz.deckbrew.buildtool.backend.common

import org.gradle.kotlin.dsl.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.configurationcache.extensions.capitalized
import xyz.deckbrew.buildtool.PluginMetadata
import xyz.deckbrew.buildtool.ext.get
import xyz.deckbrew.buildtool.ext.readJson
import xyz.deckbrew.buildtool.json.PluginJsonFormat
import xyz.deckbrew.buildtool.tasks.common.PluginStoreUploadTask
import java.io.File

abstract class BuildPipelineProvider(protected val project: Project, protected val pluginFolder: File, protected val buildDir: File) {
    /**
     * Check if this pipeline provider is applicable for the given plugin
     */
    abstract fun matches(): Boolean

    /**
     * Get the plugin metadata, for JSON files use metadataFromJson
     */
    abstract fun metadata(): PluginMetadata

    /**
     * Set up all tasks, and return the task that outputs the plugin zip
     */
    protected abstract fun TaskContainerScope.createTasks() : Task

    /**
     * Create all required tasks for this plugin
     */
    fun create() {
        val _metadata = metadata()

        // TODO in the future: Only build if version mismatch between local and plugin store

        project.tasks {
            val build by register(taskName("build")) {
                group = "deckbrew"

                getByName("allBuild").dependsOn(this)
            }

            val endTask = createTasks()

            if (System.getenv("PLUGIN_STORE_UPLOAD") == "true" && _metadata.tags.none { it == "dnu" }) {
                val upload by register<PluginStoreUploadTask>(taskName("upload")) {
                    group = "deckbrew"

                    metadata.set(_metadata)
                    pluginZip.set(endTask.outputs.files.singleFile)
                    dependsOn(endTask)
                }

                build.dependsOn(upload)
            } else {
                build.dependsOn(endTask)
            }
        }
    }

    // Utility method to get a unique task name with the given prefix
    protected fun taskName(name: String) = "${name}${sanitize(metadata().name)}"
    private fun sanitize(name: String): String {
        return name.replace(Regex("[^a-zA-Z\\d]"), "").capitalized()
    }

    // Utility method to check if the plugin folder contains a certain file
    protected fun pluginHasFile(path: String): Boolean {
        return pluginFolder.resolve(path).exists()
    }

    // Utility method to get the plugin metadata from JSON files
    protected fun metadataFromJson(): PluginMetadata {
        val packageJson = pluginFolder.resolve("package.json").readJson()
        val pluginJson = pluginFolder.resolve("plugin.json").readJson<PluginJsonFormat>()
        return PluginMetadata(
            pluginJson.name,
            pluginJson.author,
            pluginJson.publish.description,
            pluginJson.publish.tags,
            packageJson.get("version"),
            pluginJson.publish.image,
        )
    }
}
