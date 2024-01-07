plugins {
    `java-library`
    alias(stuff.plugins.kotlin.jvm)
    alias(stuff.plugins.detekt)
    alias(stuff.plugins.ksp)
}

dependencies {
    implementation(project(":kotstruct-api"))
    ksp(project(":kotstruct-generator"))
}
