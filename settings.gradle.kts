import java.net.URI

apply(from = "gradle/shared-scripts/repo-reconfiguration.settings.gradle.kts")
pluginManagement {
    repositories {
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "SpongePowered Snapshots"
            url = uri("https://repo.spongepowered.org/repository/maven-snapshots/")
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases/")
        }
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("fabric-loom") version "1.14.7"
}
dependencyResolutionManagement {
    repositories {
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org/")
        }
        maven {
            name = "PaperMC"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "EngineHub (Non-Mirrored)"
            url = URI.create("https://repo.enginehub.org/libs-release/")
            metadataSources {
                mavenPom()
                artifact()
            }
        }
        ivy {
            url = uri("https://repo.enginehub.org/language-files/")
            name = "EngineHub Language Files"
            patternLayout {
                artifact("[organisation]/[module]/[revision]/[artifact]-[revision](+[classifier])(.[ext])")
                setM2compatible(true)
            }
            metadataSources {
                artifact()
            }
            content {
                includeModuleByRegex(".*", "worldedit-lang")
            }
        }
    }
}

logger.lifecycle("""
*******************************************
 You are building WorldEdit!

 If you encounter trouble:
 1) Read COMPILING.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, ask on Discord! https://discord.gg/enginehub

 Output files will be in [subproject]/build/libs
*******************************************
""")

rootProject.name = "worldedit"

includeBuild("build-logic")

include("worldedit-libs")

listOf("1.21.4", "1.21.5", "1.21.6", "1.21.9", "1.21.11").forEach {
    include("worldedit-bukkit:adapters:adapter-$it")
}

listOf("bukkit", "core", "fabric", "neoforge", "sponge", "cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-mod")
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")

include("verification")
