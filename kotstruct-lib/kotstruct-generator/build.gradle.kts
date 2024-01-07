plugins {
    `java-library`
}

dependencies {
    implementation(stuff.ksp.api)
}

val kotlinVersion = stuff.versions.kotlin.get()
val kspVersion = stuff.versions.ksp.get()
require(kspVersion.startsWith("$kotlinVersion-")) {
    "KSP and Kotlin versions must be aligned, but are: kotlin=$kotlinVersion ksp=$kspVersion"
}
