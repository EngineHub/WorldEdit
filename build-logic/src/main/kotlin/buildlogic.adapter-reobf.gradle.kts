import buildlogic.primaryJavaReleaseVersion
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("buildlogic.adapter")
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}

crankcaseJava {
    // We use Java 21 for most of the pre-existing adapters.
    javaRelease = 21
}

java {
    // Required when we de-sync release option and declared Java versions.
    disableAutoTargetJvm()
    toolchain.languageVersion = JavaLanguageVersion.of(primaryJavaReleaseVersion)
}
