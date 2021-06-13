import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.LoomGradleExtension
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

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

apply(plugin = "fabric-loom")
apply(plugin = "java-library")

configure<LoomGradleExtension> {
    accessWidener("src/main/resources/worldedit.accesswidener")
}

val minecraftVersion = "1.17"
val yarnMappings = "1.17+build.1:v2"
val loaderVersion = "0.11.3"

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

val fabricApiConfiguration: Configuration = configurations.create("fabricApi")

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    "api"(project(":worldedit-core"))
    "implementation"(platform("org.apache.logging.log4j:log4j-bom:2.14.1") {
        because("Mojang provides Log4J at 2.14.1")
    })

    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"("net.fabricmc:yarn:$yarnMappings")
    "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")

    // [1] declare fabric-api dependency...
    "fabricApi"("net.fabricmc.fabric-api:fabric-api:0.34.9+1.17")

    // [2] Load the API dependencies from the fabric mod json...
    @Suppress("UNCHECKED_CAST")
    val fabricModJson = file("src/main/resources/fabric.mod.json").bufferedReader().use {
        groovy.json.JsonSlurper().parse(it) as Map<String, Map<String, *>>
    }
    val wantedDependencies = (fabricModJson["depends"] ?: error("no depends in fabric.mod.json")).keys
        .filter { it == "fabric-api-base" || it.contains(Regex("v\\d$")) }
        .map { "net.fabricmc.fabric-api:$it" }
    logger.lifecycle("Looking for these dependencies:")
    for (wantedDependency in wantedDependencies) {
        logger.lifecycle(wantedDependency)
    }
    // [3] and now we resolve it to pick out what we want :D
    val fabricApiDependencies = fabricApiConfiguration.incoming.resolutionResult.allDependencies
        .onEach {
            if (it is UnresolvedDependencyResult) {
                throw kotlin.IllegalStateException("Failed to resolve Fabric API", it.failure)
            }
        }
        .filterIsInstance<ResolvedDependencyResult>()
        // pick out transitive dependencies
        .flatMap {
            it.selected.dependencies
        }
        // grab the requested versions
        .map { it.requested }
        .filterIsInstance<ModuleComponentSelector>()
        // map to standard notation
        .associateByTo(
            mutableMapOf(),
            keySelector = { "${it.group}:${it.module}" },
            valueTransform = { "${it.group}:${it.module}:${it.version}" }
        )
    fabricApiDependencies.keys.retainAll(wantedDependencies)
    // sanity check
    for (wantedDep in wantedDependencies) {
        check(wantedDep in fabricApiDependencies) { "Fabric API library $wantedDep is missing!" }
    }

    fabricApiDependencies.values.forEach {
        "include"(it)
        "modImplementation"(it)
    }

    // No need for this at runtime
    "modCompileOnly"("me.lucko:fabric-permissions-api:0.1-SNAPSHOT")

    // Hook these up manually, because Fabric doesn't seem to quite do it properly.
    "compileOnly"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:fabric-loom:${project.versions.loom}")
}

configure<BasePluginConvention> {
    archivesBaseName = "$archivesBaseName-mc$minecraftVersion"
}
configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginConvention>().archivesBaseName
        artifact(tasks.named("jar")) {
            builtBy(tasks.named("remapJar"))
        }
        artifact(tasks.named("sourcesJar")) {
            builtBy(tasks.named("remapSourcesJar"))
        }
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

        include(dependency("org.antlr:antlr4-runtime"))
    }
}

tasks.register<RemapJarTask>("remapShadowJar") {
    val shadowJar = tasks.getByName<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    input.set(shadowJar.archiveFile)
    archiveFileName.set(shadowJar.archiveFileName.get().replace(Regex("-dev\\.jar$"), ".jar"))
    addNestedDependencies.set(true)
    remapAccessWidener.set(true)
}

tasks.named("assemble").configure {
    dependsOn("remapShadowJar")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        // Remove when https://github.com/gradle/gradle/issues/16555 is fixed
        suppressPomMetadataWarningsFor("runtimeElements")
    }
}
