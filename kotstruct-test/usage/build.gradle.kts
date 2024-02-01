plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)
}

dependencies {
    ksp(project(":kotstruct-generator"))
    ksp(project(":kotstruct-test:mappers"))

    implementation(project(":kotstruct-api"))
    implementation(project(":kotstruct-test:mappers"))

    testImplementation(libs.bundles.testing)
}
