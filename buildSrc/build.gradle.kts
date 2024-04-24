import java.util.Properties

plugins {
    `kotlin-dsl`
}

repositories {
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
        content {
            includeGroupAndSubgroups("io.papermc")
        }
    }
    maven {
        name = "NeoForged Maven"
        url = uri("https://maven.neoforged.net/releases")
        content {
            includeGroupAndSubgroups("net.neoforged")
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
    implementation("org.ajoberstar.grgit:grgit-gradle:5.2.2")
    implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.2")
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:5.2.0")
    implementation("org.spongepowered:spongegradle-plugin-development:2.2.0")
    implementation("org.spongepowered:vanillagradle:0.2.1-20231105.223944-69")
    val neoGradleVersion = "7.0.107"
    implementation("net.neoforged.gradle:userdev:$neoGradleVersion")
    implementation("net.neoforged.gradle:mixin:$neoGradleVersion")
    implementation("net.fabricmc:fabric-loom:$loomVersion")
    implementation("net.fabricmc:sponge-mixin:$mixinVersion")
    implementation("org.enginehub.gradle:gradle-codecov-plugin:0.2.0")
    implementation("io.papermc.paperweight.userdev:io.papermc.paperweight.userdev.gradle.plugin:1.5.13")
    constraints {
        val asmVersion = "[9.7,)"
        implementation("org.ow2.asm:asm:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.ow2.asm:asm-commons:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.vafer:jdependency:[2.10,)") {
            because("Need Java 21 support in shadow")
        }
    }
}
