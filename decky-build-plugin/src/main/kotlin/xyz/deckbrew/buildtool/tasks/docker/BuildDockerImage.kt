package xyz.deckbrew.buildtool.tasks.docker

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Task to build a docker image
 */
abstract class BuildDockerImage : DefaultTask() {
    /**
     * Directory containing the Dockerfile
     */
    @get:InputDirectory
    abstract val dockerBuildDirectory: DirectoryProperty

    /**
     * Name of the image to build
     */
    @get:Input
    abstract val imageName: Property<String>

    @TaskAction
    fun run() {
        project.exec {
            workingDir = dockerBuildDirectory.get().asFile
            commandLine("docker", "build", "-t", imageName.get(), ".")
        }
    }
}
