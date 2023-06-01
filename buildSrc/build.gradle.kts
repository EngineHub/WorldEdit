import java.util.Properties

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
        content {
            includeGroupByRegex("io\\.papermc\\..*")
        }
    }
    maven {
        name = "Forge Maven"
        url = uri("https://maven.minecraftforge.net/")
        content {
            includeGroupByRegex("net\\.minecraftforge(|\\..*)$")
        }
    }
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/maven")
    }
    maven {
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
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
    implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.6.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.1.1")
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.32.0")
    implementation("org.spongepowered:spongegradle-plugin-development:2.0.2")
    implementation("org.spongepowered:vanillagradle:0.2.1-20220619.210040-41")
    implementation("net.minecraftforge.gradle:ForgeGradle:6.0.4")
    implementation("net.fabricmc:fabric-loom:$loomVersion")
    implementation("net.fabricmc:sponge-mixin:$mixinVersion")
    implementation("org.enginehub.gradle:gradle-codecov-plugin:0.1.0")
    implementation("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:1.5.5")
    implementation("org.spongepowered:mixingradle:0.7.33")
}
