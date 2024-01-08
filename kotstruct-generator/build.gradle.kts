plugins {
    `java-library`
}

dependencies {
    implementation(project(":kotstruct-api"))
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(libs.compile.testing.ksp)
    testImplementation(libs.bundles.testing)
}

val kotlinVersion = libs.versions.kotlin.get()
val kspVersion = libs.versions.ksp.get()
require(kspVersion.startsWith("$kotlinVersion-")) {
    "KSP and Kotlin versions must be aligned, but are: kotlin=$kotlinVersion ksp=$kspVersion"
}
