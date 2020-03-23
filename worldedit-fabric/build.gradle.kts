import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

apply(plugin = "fabric-loom")

val minecraftVersion = "1.15.2"
val yarnMappings = "1.15.2+build.8:v2"
val loaderVersion = "0.7.6+build.180"

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

dependencies {
    "compile"(project(":worldedit-core"))
    "compile"("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")

    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"("net.fabricmc:yarn:$yarnMappings")
    "modCompile"("net.fabricmc:fabric-loader:$loaderVersion")

    listOf(
        "net.fabricmc.fabric-api:fabric-api-base:0.1.2+28f8190f42",
        "net.fabricmc.fabric-api:fabric-events-interaction-v0:0.2.6+12515ed975",
        "net.fabricmc.fabric-api:fabric-events-lifecycle-v0:0.1.2+b7f9825de8",
        "net.fabricmc.fabric-api:fabric-networking-v0:0.1.7+12515ed975"
    ).forEach {
        "include"(it)
        "modImplementation"(it)
    }

    // Hook these up manually, because Fabric doesn't seem to quite do it properly.
    "compileClasspath"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:sponge-mixin:${project.versions.mixin}")
    "annotationProcessor"("net.fabricmc:fabric-loom:${project.versions.loom}")

    "testCompile"("org.mockito:mockito-core:1.9.0-rc1")
}

configure<BasePluginConvention> {
    archivesBaseName = "$archivesBaseName-mc$minecraftVersion"
}

tasks.named<Copy>("processResources") {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.ext["internalVersion"])

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.ext["internalVersion"])
    }

    // copy everything else except the mod json
    from(sourceSets["main"].resources.srcDirs) {
        exclude("fabric.mod.json")
    }
}

addJarManifest(includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dist-dev")
    dependencies {
        relocate("org.slf4j", "com.sk89q.worldedit.slf4j")
        relocate("org.apache.logging.slf4j", "com.sk89q.worldedit.log4jbridge")
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")

        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
        include(dependency("org.antlr:antlr4-runtime"))
    }
}

tasks.register<Jar>("deobfJar") {
    from(sourceSets["main"].output)
    archiveClassifier.set("dev")
}

artifacts {
    add("archives", tasks.named("deobfJar"))
}

tasks.register<RemapJarTask>("remapShadowJar") {
    val shadowJar = tasks.getByName<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    input.set(shadowJar.archiveFile)
    archiveFileName.set(shadowJar.archiveFileName.get().replace(Regex("-dev\\.jar$"), ".jar"))
    addNestedDependencies.set(true)
}

tasks.named("assemble").configure {
    dependsOn("remapShadowJar")
}
