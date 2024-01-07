rootProject.name = "kotstruct-test"

dependencyResolutionManagement {
    versionCatalogs {
        create("stuff") {
            from(files("../gradle/stuff.versions.toml"))
        }
    }
}

includeBuild("../kotstruct-lib") {
    dependencySubstitution {
        substitute(module("io.github.vooft:kotstruct-api")).using(project(":kotstruct-api"))
    }
}
