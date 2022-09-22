package xyz.deckbrew.buildtool.tasks.docker

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*
import xyz.deckbrew.buildtool.ext.exists
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Run a docker container
 */
abstract class DockerRunTask : DefaultTask() {
    /**
     * The docker image to run
     */
    @get:Input
    abstract val image: Property<String>

    /**
     * Mounts to add to the container
     */
    @get:Input
    abstract val mounts: MapProperty<String, String>

    /**
     * Directory to run from
     */
    @get:InputDirectory
    abstract val runDirectory: DirectoryProperty

    /**
     * Additional arguments to pass to the docker run command
     */
    @get:Input
    abstract val arguments: ListProperty<String>

    @TaskAction
    fun run() {
        val directory by runDirectory

        val _cmdline = mutableListOf(
            "docker",
            "run",
            "--rm",
            "-i",
        )

        // Add mounts
        for ((src, dst) in mounts.get().entries) {
            File(src).mkdirs()
            _cmdline.add("-v")
            _cmdline.add("$src:$dst")
        }

        // Add arguments
        for (arg in arguments.get()) {
            _cmdline.add(arg)
        }

        _cmdline.add(image.get())

        println("Running docker command: ${_cmdline.joinToString(" ")}")

        project.exec {
            workingDir = directory.asFile
            commandLine(_cmdline)
        }
    }
}
