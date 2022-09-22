plugins {
    id("decky-build-plugin")
}

tasks {
    create("build") {
        dependsOn("allBuild")
    }
}
