plugins {
    `java-library`
}

gradle.includedBuilds.forEach {
    tasks.getByName("test").dependsOn += it.task(":test")
    tasks.getByName("clean").dependsOn += it.task(":clean")
}
