plugins {
    kotlin("jvm") version "1.9.21"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

group = "io.github.vooft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

