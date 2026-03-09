import buildlogic.addEngineHubRepository
import buildlogic.stringyLibs
import buildlogic.getVersion

plugins {
    `java-library`
    id("buildlogic.common")
    id("buildlogic.common-java")
    id("io.papermc.paperweight.userdev")
}

java {
    // Required when we de-sync release option and declared Java versions.
    disableAutoTargetJvm()
}

tasks
    .withType<JavaCompile>()
    .matching { it.name == "compileJava" || it.name == "compileTestJava" }
    .configureEach {
        // We use Java 21 for most of the pre-existing adapters.
        options.release.set(21)
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
