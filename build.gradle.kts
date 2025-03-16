@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.13"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.13")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}

benchmark {
    configurations {
        named("main")
    }
    targets {
        register("main")
    }
}