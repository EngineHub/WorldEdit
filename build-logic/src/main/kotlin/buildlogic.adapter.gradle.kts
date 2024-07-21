import buildlogic.stringyLibs
import buildlogic.getVersion

plugins {
    `java-library`
    id("buildlogic.common")
    id("buildlogic.common-java")
    id("io.papermc.paperweight.userdev")
}

configure<buildlogic.CommonJavaExtension> {
    banSlf4j = false
}

paperweight {
    injectPaperRepository = false
}

repositories {
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
    mavenCentral()
    afterEvaluate {
        killNonEngineHubRepositories()
    }
}

dependencies {
    "implementation"(project(":worldedit-bukkit"))
    constraints {
        "remapper"("net.fabricmc:tiny-remapper:[${stringyLibs.getVersion("minimumTinyRemapper")},)") {
            because("Need remapper to support Java 21")
        }
    }
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}
