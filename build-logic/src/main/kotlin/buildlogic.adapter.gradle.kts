import buildlogic.addEngineHubRepository
import buildlogic.stringyLibs
import buildlogic.getVersion

plugins {
    `java-library`
    id("buildlogic.common")
    id("buildlogic.common-java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven {
        name = "Minecraft Libraries"
        url = uri("https://libraries.minecraft.net/")
    }
    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "FabricMC (Yarn)"
        url = uri("https://maven.fabricmc.net/#yarn-only")
    }
    maven {
        name = "SpongePowered Releases"
        url = uri("https://repo.spongepowered.org/repository/maven-releases/")
    }
    maven {
        name = "SpongePowered Snapshots"
        url = uri("https://repo.spongepowered.org/repository/maven-snapshots/")
    }
    addEngineHubRepository()
}

dependencies {
    "implementation"(project(":worldedit-bukkit"))
    constraints {
        "remapper"("net.fabricmc:tiny-remapper:[${stringyLibs.getVersion("minimumTinyRemapper")},)") {
            because("Need remapper to support Java 21")
        }
    }
}
