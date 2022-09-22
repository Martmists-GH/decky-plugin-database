package xyz.deckbrew.buildtool.tasks.common

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.getValue
import xyz.deckbrew.buildtool.PluginMetadata
import java.io.File

/**
 * Task to upload a plugin to the DeckBrew plugin repository.
 */
abstract class PluginStoreUploadTask : Exec() {
    /**
     * Zip file to upload
     */
    @get:InputFile
    abstract val pluginZip: Property<File>

    /**
     * Plugin Metadata
     */
    @get:Input
    abstract val metadata: Property<PluginMetadata>

    init {
        val key = System.getenv("PLUGIN_STORE_KEY")
        val zipFile by pluginZip
        val meta by metadata

        // Copied from original script; consider rewriting with java code
        commandLine(
            "curl",
            "-X", "POST",
            "-H", "Authorization: $key",
            "-F", "name=${meta.name}",
            "-F", "author=${meta.author}",
            "-F", "description=${meta.description}",
            "-F", "tags=${meta.tags.joinToString(",")}",
            "-F", "version_name=${meta.version}",
            "-F", "image=${meta.image}",
            "-F", "file=@${zipFile.absolutePath}", "https://plugins.gradle.org/api/plugin")
    }
}
