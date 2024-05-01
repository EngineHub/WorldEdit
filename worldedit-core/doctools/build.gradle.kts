plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("buildlogic.common")
}

application.mainClass.set("com.sk89q.worldedit.internal.util.DocumentationPrinter")
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

dependencies {
    "implementation"(project(":worldedit-libs:core:ap"))
    "implementation"(project(":worldedit-core"))
    "implementation"(kotlin("stdlib-jdk8"))
    "implementation"(kotlin("reflect"))
    "implementation"(libs.guava)
}
