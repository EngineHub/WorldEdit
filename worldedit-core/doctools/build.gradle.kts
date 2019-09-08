import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    application
}

applyCommonConfiguration()

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application.mainClassName = "com.sk89q.worldedit.internal.util.DocumentationPrinter"
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

dependencies {
    "implementation"(project(":worldedit-libs:core:ap"))
    "implementation"(project(":worldedit-core"))
    "implementation"(kotlin("stdlib-jdk8"))
    "implementation"(kotlin("reflect"))
}
