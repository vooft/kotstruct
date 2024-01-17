plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)
}

dependencies {
    ksp(project(":kotstruct-generator"))
    ksp(project(":kotstruct-example:mappers"))

    implementation(project(":kotstruct-api"))
    implementation(project(":kotstruct-example:mappers"))

    testImplementation(libs.bundles.testing)
}
