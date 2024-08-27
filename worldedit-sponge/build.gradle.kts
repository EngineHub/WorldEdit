import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    alias(libs.plugins.sponge.spongegradle)
    id("org.spongepowered.gradle.vanilla")
    `java-library`
    id("buildlogic.platform")
}

platform {
    kind = buildlogic.WorldEditKind.Mod
    includeClasspath = true
}

minecraft {
    injectRepositories(false)
    version(libs.versions.sponge.minecraft.get())
}

repositories {
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
    mavenCentral()
    verifyEngineHubRepositories()
    afterEvaluate {
        verifyEngineHubRepositories()
    }
}

sponge {
    injectRepositories(false)
    apiVersion(libs.versions.sponge.api.asProvider().get())
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
    "api"(project(":worldedit-core"))
    "api"(project(":worldedit-libs:sponge"))

    "api"("org.apache.logging.log4j:log4j-api")
    "implementation"("org.bstats:bstats-sponge:3.0.0")
    "implementation"("it.unimi.dsi:fastutil")
    "testImplementation"(libs.mockito.core)

    // Silence some warnings, since apparently this isn't on the compile classpath like it should be.
    "compileOnly"("com.google.errorprone:error_prone_annotations:2.11.0")
}

configure<BasePluginExtension> {
    archivesName.set("${project.name}-api${libs.versions.sponge.api.major.get()}")
}

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
