plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
}

group = "org.springframework"
version = "1.0.0"

repositories {
    mavenCentral()
}

val kotestVersion = "5.3.2"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation(kotlin("reflect"))

    testImplementation("io.kotest:kotest-runner-junit5:${kotestVersion}")
    testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
    testImplementation("io.kotest:kotest-property:${kotestVersion}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}