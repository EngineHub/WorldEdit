import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import kotlin.reflect.KClass

buildscript {
    repositories {
        jcenter()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "sponge"
            url = uri("https://repo.spongepowered.org/maven")
        }
    }

    dependencies {
        "classpath"("net.fabricmc:fabric-loom:0.2.3-SNAPSHOT")
        "classpath"("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    }
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

apply(plugin = "fabric-loom")

val minecraftVersion = "1.14.4"
val fabricVersion = "0.3.0+build.200"
val yarnMappings = "1.14.4+build.1"
val loaderVersion = "0.4.8+build.155"

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

    "modCompile"("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

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

tasks.named<Jar>("jar") {
    manifest {
        attributes("Class-Path" to "truezip.jar WorldEdit/truezip.jar js.jar WorldEdit/js.jar",
                   "WorldEdit-Version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("dist-dev")
    dependencies {
        relocate("org.slf4j", "com.sk89q.worldedit.slf4j")
        relocate("org.apache.logging.slf4j", "com.sk89q.worldedit.log4jbridge")

        include(dependency("org.slf4j:slf4j-api"))
        include(dependency("org.apache.logging.log4j:log4j-slf4j-impl"))
    }
}

tasks.register<Jar>("deobfJar") {
    from(sourceSets["main"].output)
    archiveClassifier.set("dev")
}

artifacts {
    add("archives", tasks.named("deobfJar"))
}

// intellij has trouble detecting RemapJarTask as a subclass of Task
@Suppress("UNCHECKED_CAST")
val remapJarIntellijHack = RemapJarTask::class as KClass<Task>
tasks.register("remapShadowJar", remapJarIntellijHack) {
    (this as RemapJarTask).run {
        val shadowJar = tasks.getByName<ShadowJar>("shadowJar")
        dependsOn(shadowJar)
        setInput(shadowJar.archiveFile)
        setOutput(shadowJar.archiveFile.get().asFile.absolutePath.replace(Regex("-dev\\.jar$"), ".jar"))
    }
}

tasks.named("assemble").configure {
    dependsOn("remapShadowJar")
}
