package xyz.deckbrew.buildtool.backend.rollup

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*
import xyz.deckbrew.buildtool.backend.common.BuildPipelineProvider
import xyz.deckbrew.buildtool.ext.exists
import xyz.deckbrew.buildtool.tasks.docker.BuildDockerImage
import xyz.deckbrew.buildtool.tasks.docker.DockerRunTask
import java.io.File

class RollupPipelineProvider(project: Project, pluginFolder: File, buildDir: File) :
    BuildPipelineProvider(project, pluginFolder, buildDir) {

    override fun matches() = pluginHasFile("rollup.config.js")
    override fun metadata() = metadataFromJson()

    override fun TaskContainerScope.createTasks() : Task {
        val metadata = metadata()

        // Build frontend
        val frontend by register<DockerRunTask>(taskName("buildFrontend")) {
            group = "deckbrew"

            image.set("ghcr.io/steamdeckhomebrew/builder:latest")
            mounts.put(pluginFolder.absolutePath, "/plugin")
            mounts.put(buildDir.resolve("frontend").absolutePath, "/out")
            runDirectory.set(pluginFolder)

            outputs.dir(buildDir.resolve("frontend/dist"))
        }

        // Copy files to match expected structure
        val copy by register<Copy>(taskName("copyAssets")) {
            group = "deckbrew"

            destinationDir = buildDir.resolve("bundle")
            includeEmptyDirs = false

            into(metadata.name) {
                from(pluginFolder) {
                    include(
                        "LICENSE*",
                        "README*",
                        "CHANGELOG*",
                        "package.json",
                        "plugin.json",
                        "**/*.py",
                        "**/*.so",
                        "data/**",
                    )
                }

                into("dist") {
                    from(frontend)
                    into("assets") {
                        from(pluginFolder.resolve("assets"))
                    }
                }
            }
        }

        // Optional: Build backend
        val backendFolder = pluginFolder.resolve("backend")
        if (backendFolder.exists()) {
            // Build backend
            val backend by register<DockerRunTask>(taskName("buildBackend")) {
                group = "deckbrew"

                image.set("ghcr.io/steamdeckhomebrew/holo-base:latest")
                mounts.put(backendFolder.absolutePath, "/backend")
                mounts.put(buildDir.resolve("bin").absolutePath, "/backend/out")
                runDirectory.set(backendFolder)

                // Set to use entrypoint.sh if it exists
                if (backendFolder.exists("entrypoint.sh")) {
                    arguments.set(listOf("--entrypoint", "/backend/entrypoint.sh"))
                }

                outputs.dir(buildDir.resolve("bin"))
            }

            // Add backend to copy output
            copy.apply {
                into("${metadata.name}/bin") {
                    from(backend)
                }
            }

            // Optional: Build docker image
            if (backendFolder.exists("Dockerfile")) {
                val dockerImageName = "docker-backend-${backend.name}".toLowerCase()

                val backendImage by register<BuildDockerImage>(taskName("buildBackendImage")) {
                    group = "deckbrew"

                    dockerBuildDirectory.set(pluginFolder.resolve("backend"))
                    imageName.set(dockerImageName)
                }

                // Set backend build to use docker image
                backend.apply {
                    dependsOn(backendImage)
                    image.set(dockerImageName)
                }
            }
        }

        // Optional: Python packages
        if (pluginFolder.exists("requirements.txt")) {
            val python by register<Exec>(taskName("buildPython")) {
                group = "deckbrew"
                val dir = buildDir.resolve("python-packages")

                workingDir = pluginFolder
                commandLine("pip", "install", "-r", "requirements.txt", "-t", dir.absolutePath)

                outputs.dir(dir)
            }

            // Add python to copy output
            copy.apply {
                from(python)
            }
        }

        // Create zip from copied files
        val zip by register<Zip>(taskName("zip")) {
            group = "deckbrew"

            destinationDirectory.set(buildDir.resolve("bundled"))
            archiveFileName.set("${metadata.name}-${metadata.version}.zip")
            from(copy)
        }

        return zip
    }
}
