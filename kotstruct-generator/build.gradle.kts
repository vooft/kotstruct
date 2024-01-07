plugins {
    `java-library`
}

dependencies {
    implementation(project(":kotstruct-api"))
    implementation(stuff.ksp.api)

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

val kotlinVersion = stuff.versions.kotlin.get()
val kspVersion = stuff.versions.ksp.get()
require(kspVersion.startsWith("$kotlinVersion-")) {
    "KSP and Kotlin versions must be aligned, but are: kotlin=$kotlinVersion ksp=$kspVersion"
}
