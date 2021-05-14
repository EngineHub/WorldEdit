import java.util.Properties

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "Forge Maven"
        url = uri("https://maven.minecraftforge.net/")
    }
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
    implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.6.0")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.1.0")
    implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.21.0")
    implementation("org.spongepowered:SpongeGradle:0.11.5")
    implementation("net.minecraftforge.gradle:ForgeGradle:4.1.12")
    implementation("net.fabricmc:fabric-loom:$loomVersion")
    implementation("net.fabricmc:sponge-mixin:$mixinVersion")
    implementation("org.enginehub.gradle:gradle-codecov-plugin:0.1.0")
}
