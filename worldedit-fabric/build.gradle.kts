import buildlogic.internalVersion
import buildlogic.withCuiProtocolDependsOnCommonRule
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RunGameTask
import org.gradle.kotlin.dsl.attributes
import java.net.URI

plugins {
    alias(libs.plugins.fabric.loom)
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

repositories {
    maven {
        name = "EngineHub (Non-Mirrored)"
        url = URI.create("https://repo.enginehub.org/libs-release/")
        metadataSources {
            gradleMetadata()
            mavenPom()
            artifact()
        }
    }
}

withCuiProtocolDependsOnCommonRule(libs.cuiProtocol.fabric.get().module)

dependencies {
    "api"(project(":worldedit-core"))

    "minecraft"(libs.fabric.minecraft)
    "implementation"(libs.fabric.loader)
    "implementation"(libs.cuiProtocol.fabric)
    "include"(libs.cuiProtocol.fabric) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class, Bundling.SHADOWED))
        }
    }

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
    "compileOnly"(libs.fabric.permissions.api)

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
    // Use the JAR output, not classes, as we need jar-in-jar from loom to work properly.
    dependsOn(tasks.jar)
    val processedFabricModJson = layout.buildDirectory.file("resources/main/fabric.mod.json").get().asFile.absoluteFile
    eachFile {
        // Exclude the fabric.mod.json in the resources folder to allow the one from Fabric's jar task to be added
        if (path == "fabric.mod.json" && file.absoluteFile == processedFabricModJson) {
            exclude()
        }
    }
    from(zipTree(tasks.jar.flatMap { it.archiveFile }))
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
