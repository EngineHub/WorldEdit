plugins {
    `kotlin-dsl`
}

repositories {
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

dependencies {
    implementation(gradleApi())
    implementation(libs.levelHeadered)
    implementation(libs.japicmp)
    implementation(libs.shadow)
    implementation(libs.jfrog.buildinfo)
    implementation(libs.paperweight)
    implementation(libs.errorprone.gradle.plugin)
    implementation(libs.gson)

    implementation(libs.sponge.vanillagradle)
    implementation(libs.neogradle.neoform)

    constraints {
        val asmVersion = "[${libs.versions.minimumAsm.get()},)"
        implementation("org.ow2.asm:asm:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.ow2.asm:asm-commons:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.vafer:jdependency:[${libs.versions.minimumJdependency.get()},)") {
            because("Need Java 21 support in shadow")
        }
    }
}
