rootProject.name = "kotstruct-lib"

include("kotstruct-api")
include("kotstruct-generator")

dependencyResolutionManagement {
    versionCatalogs {
        create("stuff") {
            from(files("../gradle/stuff.versions.toml"))
        }
    }
}
