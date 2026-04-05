import buildlogic.addEngineHubRepository

plugins {
    alias(libs.plugins.fabric.loom)
    `java-library`
    id("buildlogic.core-and-platform")
}

description = "The Minecraft-specific implementation of WorldEdit's core module." +
        " This code is shared by all platforms that rely on access without an API."

repositories {
    addEngineHubRepository()
}

loom {
    accessWidenerPath.set(project.file("src/main/resources/worldedit.accesswidener"))
}

dependencies {
    "api"(project(":worldedit-core"))

    "minecraft"(libs.fabric.minecraft)
    "implementation"(libs.fabric.loader)

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    "compileOnly"(libs.errorprone.annotations)
}

base {
    archivesName.set("${project.name}-mc${libs.fabric.minecraft.get().version}")
}

publishing {
    publications.named<MavenPublication>("maven") {
        artifactId = base.archivesName.get()
        from(components["java"])
    }
}
