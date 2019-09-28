import java.util.Properties

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
    maven {
        name = "Forge Maven"
        url = uri("https://files.minecraftforge.net/maven")
    }
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/maven")
    }
}

configurations.all {
    resolutionStrategy {
        // Fabric needs this.
        force(
            "commons-io:commons-io:2.5",
            "org.ow2.asm:asm:7.1",
            "org.ow2.asm:asm-commons:7.1"
        )
    }
}

val properties = Properties().also { props ->
    project.projectDir.resolveSibling("gradle.properties").bufferedReader().use {
        props.load(it)
    }
}
val loomVersion: String = properties.getProperty("loom.version")
val mixinVersion: String = properties.getProperty("mixin.version")

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:3.1.1")
    implementation("com.github.jengelman.gradle.plugins:shadow:5.1.0")
    implementation("net.ltgt.apt-eclipse:net.ltgt.apt-eclipse.gradle.plugin:0.21")
    implementation("net.ltgt.apt-idea:net.ltgt.apt-idea.gradle.plugin:0.21")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.9.7")
    implementation("gradle.plugin.org.spongepowered:spongegradle:0.9.0")
    implementation("net.minecraftforge.gradle:ForgeGradle:3.0.143")
    implementation("net.fabricmc:fabric-loom:$loomVersion")
    implementation("net.fabricmc:sponge-mixin:$mixinVersion")
}
