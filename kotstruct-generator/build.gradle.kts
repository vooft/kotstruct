plugins {
    `java-library`
}

dependencies {
    implementation(project(":kotstruct-api"))
    implementation(stuff.ksp.api)
    implementation(stuff.kotlinpoet.ksp)

    testImplementation(stuff.compile.testing.ksp)
    testImplementation(stuff.bundles.testing)
}

val kotlinVersion = stuff.versions.kotlin.get()
val kspVersion = stuff.versions.ksp.get()
require(kspVersion.startsWith("$kotlinVersion-")) {
    "KSP and Kotlin versions must be aligned, but are: kotlin=$kotlinVersion ksp=$kspVersion"
}
