plugins {
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
}
