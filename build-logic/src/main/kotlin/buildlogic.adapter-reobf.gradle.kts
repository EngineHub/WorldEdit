import buildlogic.ArtifactPriority
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import io.papermc.paperweight.util.constants.REOBF_CONFIG

plugins {
    id("buildlogic.adapter")
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}

configurations.named(REOBF_CONFIG) {
    attributes {
        attribute(
            ArtifactPriority.ATTRIBUTE,
            objects.named(ArtifactPriority.PRIMARY),
        )
    }
}
