import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("buildlogic.adapter")
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}
