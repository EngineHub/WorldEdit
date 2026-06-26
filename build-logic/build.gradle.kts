plugins {
    `kotlin-dsl`
}

repositories {
    maven {
        name = "SpongePowered"
        url = uri("https://repo.spongepowered.org/repository/maven-releases/")
    }
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases/")
    }
    maven {
        name = "MinecraftForge"
        url = uri("https://maven.minecraftforge.net/")
    }
    maven {
        name = "EngineHub"
        url = uri("https://repo.enginehub.org/libs-release/")
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.crankcase.java)
    implementation(libs.crankcase.javaLibrary)
    implementation(libs.crankcase.checkstyle)
    implementation(libs.crankcase.common)
    implementation(libs.crankcase.licensing)
    implementation(libs.crankcase.git)
    implementation(libs.crankcase.japicmp)
    implementation(libs.crankcase.publishing)
    implementation(libs.levelHeadered)
    implementation(libs.shadow)
    implementation(libs.paperweight)
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
