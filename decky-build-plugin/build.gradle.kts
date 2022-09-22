plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "1.6.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        create("decky-build") {
            id = "decky-build-plugin"
            implementationClass = "xyz.deckbrew.buildtool.DeckbrewBuildPlugin"
        }
    }
}
