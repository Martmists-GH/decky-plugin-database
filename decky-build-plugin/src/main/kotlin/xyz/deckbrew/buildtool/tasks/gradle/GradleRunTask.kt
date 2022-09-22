package xyz.deckbrew.buildtool.tasks.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File

abstract class GradleRunTask : DefaultTask() {
    @get:Input
    abstract val task: ListProperty<String>

    @get:InputDirectory
    abstract val runDirectory: DirectoryProperty

    @TaskAction
    fun run() {
        // TODO: run in docker
        //  However, the gradle daemon is already running in a container, so it'd be faster to
        //  just run the other tasks in the same container
        project.exec {
            workingDir = runDirectory.get().asFile
            commandLine("./gradlew", *task.get().toTypedArray())
        }
    }
}
