import buildlogic.internalVersion
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RunGameTask

plugins {
    id("net.fabricmc.fabric-loom") version "1.15.4"
    `java-library`
    id("buildlogic.platform")
}

platform {
    kind = buildlogic.WorldEditKind.Mod
    includeClasspath = true
}

val fabricApiConfiguration: Configuration = configurations.create("fabricApi")

loom {
    accessWidenerPath.set(project.file("src/main/resources/worldedit.accesswidener"))
}

tasks.withType<RunGameTask>().configureEach {
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
}

dependencies {
    "api"(project(":worldedit-core"))

    "minecraft"(libs.fabric.minecraft)
    "implementation"(libs.fabric.loader)
    //"include"(libs.cuiProtocol.fabric)
    //"implementation"(libs.cuiProtocol.fabric)

    // [1] Load the API dependencies from the fabric mod json...
    @Suppress("UNCHECKED_CAST")
    val fabricModJson = file("src/main/resources/fabric.mod.json").bufferedReader().use {
        groovy.json.JsonSlurper().parse(it) as Map<String, Map<String, *>>
    }
    val wantedDependencies = (fabricModJson["depends"] ?: error("no depends in fabric.mod.json")).keys
        .filter { it == "fabric-api-base" || it.contains(Regex("v\\d$")) }
        .toSet()
    // [2] Request the matching dependency from fabric-loom
    for (wantedDependency in wantedDependencies) {
        val dep = fabricApi.module(wantedDependency, libs.versions.fabric.api.get())
        "include"(dep)
        "implementation"(dep)
    }

    // No need for this at runtime
    //"compileOnly"(libs.fabric.permissions.api)

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    "compileOnly"(libs.errorprone.annotations)
}

configure<BasePluginExtension> {
    archivesName.set("${project.name}-mc${libs.fabric.minecraft.get().version}")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}

tasks.named<Copy>("processResources") {
    // Avoid carrying project reference into task execution
    val internalVersion = project.internalVersion
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", internalVersion)
    filesMatching("fabric.mod.json") {
        this.expand(mapOf("version" to internalVersion.get()))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dist")
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        relocate("net.royawesome.jlibnoise", "com.sk89q.worldedit.jlibnoise")

        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("com.sk89q.lib:jlibnoise"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
