plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":kotstruct-api"))
    ksp(project(":kotstruct-generator"))

    testImplementation(libs.bundles.testing)
}
