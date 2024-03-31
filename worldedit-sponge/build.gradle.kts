import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin")
    id("org.spongepowered.gradle.vanilla")
}

applyPlatformAndCoreConfiguration(javaRelease = 17)
applyShadowConfiguration()

repositories {
    mavenCentral()
}

minecraft {
    version("1.20")
}

val spongeApiVersion = "11.0.0-SNAPSHOT";

sponge {
    apiVersion(spongeApiVersion)
    license("GPL-3.0-or-later")
    plugin("worldedit") {
        loader {
            name(PluginLoaders.JAVA_PLAIN)
            version("1.0")
        }
        displayName("WorldEdit")
        version(project.ext["internalVersion"].toString())
        entrypoint("com.sk89q.worldedit.sponge.SpongeWorldEdit")
        description("WorldEdit is an easy-to-use in-game world editor for Minecraft, supporting both single- and multi-player.")
        links {
            homepage("https://enginehub.org/worldedit/")
            source("https://github.com/EngineHub/WorldEdit")
            issues("https://github.com/EngineHub/WorldEdit/issues")
        }
        contributor("EngineHub") {
            description("Various members of the EngineHub team")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

dependencies {
    api(project(":worldedit-core"))
    api(project(":worldedit-libs:sponge"))

    implementation(platform("org.apache.logging.log4j:log4j-bom:${Versions.LOG4J}") {
        because("Sponge 8 (will?) provides Log4J")
    })
    api("org.apache.logging.log4j:log4j-api")
    implementation("org.bstats:bstats-sponge:3.0.0")
    implementation("it.unimi.dsi:fastutil")
    testImplementation("org.mockito:mockito-core:${Versions.MOCKITO}")

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    compileOnly("com.google.errorprone:error_prone_annotations:2.11.0")
}

configure<BasePluginExtension> {
    archivesName.set("${project.name}-api$spongeApiVersion")
}

addJarManifest(WorldEditKind.Mod, includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency("org.bstats:"))
        include(dependency("org.antlr:antlr4-runtime"))
        include(dependency("com.sk89q.lib:jlibnoise"))

        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        relocate("org.bstats", "com.sk89q.worldedit.sponge.bstats")
        relocate("net.royawesome.jlibnoise", "com.sk89q.worldedit.jlibnoise")
    }
}
tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
