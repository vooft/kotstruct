plugins {
    `java-library`
    alias(stuff.plugins.kotlin.jvm)
    alias(stuff.plugins.detekt)
}

dependencies {
    implementation("io.github.vooft:kotstruct-api")
}
