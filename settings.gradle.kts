rootProject.name = "kotstruct"

include("kotstruct-api")
include("kotstruct-generator")
include("kotstruct-test")

dependencyResolutionManagement {
    versionCatalogs {
        create("stuff") {
            from(files("./gradle/stuff.versions.toml"))
        }
    }
}
