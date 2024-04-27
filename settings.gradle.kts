pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "worldedit"

include("worldedit-libs")

listOf("1.19.4", "1.20", "1.20.2", "1.20.4", "1.20.5").forEach {
    include("worldedit-bukkit:adapters:adapter-$it")
}

listOf("bukkit", "core", "fabric", "neoforge", "cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-mod")
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")

include("verification")
