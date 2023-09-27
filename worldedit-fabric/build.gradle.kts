import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.configuration.FabricApiExtension
import net.fabricmc.loom.task.RemapJarTask

buildscript {
    repositories {
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:${versions.loom}")
    }
}

applyPlatformAndCoreConfiguration(javaRelease = 17)
applyShadowConfiguration()

apply(plugin = "fabric-loom")
apply(plugin = "java-library")

val minecraftVersion = "1.20.2"
val loaderVersion = "0.14.22"

val fabricApiConfiguration: Configuration = configurations.create("fabricApi")

configure<LoomGradleExtensionAPI> {
    accessWidenerPath.set(project.file("src/main/resources/worldedit.accesswidener"))
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    getByName("Mojang") {
        content {
            includeGroupByRegex("com\\.mojang\\..*")
        }
    }
}

dependencies {
    "api"(project(":worldedit-core"))
    "implementation"(platform("org.apache.logging.log4j:log4j-bom:${Versions.LOG4J}") {
        because("Mojang provides Log4J")
    })

    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"(project.the<LoomGradleExtensionAPI>().officialMojangMappings())
    "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")


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
        val dep = project.the<FabricApiExtension>().module(wantedDependency, "0.89.1+1.20.2")
        "include"(dep)
        "modImplementation"(dep)
    }

    // No need for this at runtime
    "modCompileOnly"("me.lucko:fabric-permissions-api:0.1-SNAPSHOT")

    // Hook these up manually, because Fabric doesn't seem to quite do it properly.
    "compileOnly"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:fabric-loom:${project.versions.loom}")

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    "compileOnly"("com.google.errorprone:error_prone_annotations:2.11.0")
}

configure<BasePluginExtension> {
    archivesName.set("${project.name}-mc$minecraftVersion")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.ext["internalVersion"])
    filesMatching("fabric.mod.json") {
        this.expand("version" to project.ext["internalVersion"])
    }
}

addJarManifest(WorldEditKind.Mod, includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dist-dev")
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        relocate("net.royawesome.jlibnoise", "com.sk89q.worldedit.jlibnoise")

        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("com.sk89q.lib:jlibnoise"))
    }
}

tasks.register<RemapJarTask>("remapShadowJar") {
    val shadowJar = tasks.getByName<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    inputFile.set(shadowJar.archiveFile)
    archiveFileName.set(shadowJar.archiveFileName.get().replace(Regex("-dev\\.jar$"), ".jar"))
    addNestedDependencies.set(true)
}

tasks.named("assemble").configure {
    dependsOn("remapShadowJar")
}
