import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    `maven-publish`
}

allprojects {
    repositories {
        mavenCentral()
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.gradle.maven-publish")

    group = property("group") ?: "com.github.vooft.kotstruct"
    version = property("version") ?: "1.0-SNAPSHOT"

    detekt {
        buildUponDefaultConfig = true
        config.from(files("$rootDir/detekt.yml"))
        basePath = rootDir.absolutePath
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
            showStandardStreams = true
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
            jvmTarget = "21"
        }
    }
}


publishing {
    listOf(project(":kotstruct-api"), project(":kotstruct-generator")).forEach { sub ->
        publications {
            create<MavenPublication>("${sub.name}-maven") {
                println("Configuring artifact for ${sub.group}:${sub.name}:${sub.version}")

                groupId = sub.group.toString()
                artifactId = sub.name
                version = sub.version.toString()
                from(sub.components["java"])
            }
        }
    }


    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
