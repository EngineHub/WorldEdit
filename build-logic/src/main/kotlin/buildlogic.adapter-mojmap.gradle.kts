import buildlogic.ArtifactPriority
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.kotlin.dsl.named

plugins {
    id("buildlogic.adapter")
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

configurations.named("runtimeElements") {
    attributes {
        attribute(
            ArtifactPriority.ATTRIBUTE,
            objects.named(ArtifactPriority.PRIMARY),
        )
    }
}
