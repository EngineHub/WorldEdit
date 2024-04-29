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
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.licenser)
    implementation(libs.grgit)
    implementation(libs.japicmp)
    implementation(libs.shadow)
    implementation(libs.jfrog.buildinfo)
    implementation(libs.neoGradle.userdev)
    implementation(libs.fabric.loom)
    implementation(libs.fabric.mixin)
    implementation(libs.codecov)
    implementation(libs.paperweight)
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
