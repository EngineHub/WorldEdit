import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.kotlin.dsl.named

plugins {
    id("buildlogic.adapter")
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

tasks
    .withType<JavaCompile>()
    .matching { it.name == "compileJava" || it.name == "compileTestJava" }
    .configureEach {
        options.release.set(25)
    }
