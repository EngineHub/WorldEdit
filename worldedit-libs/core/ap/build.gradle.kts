plugins {
    id("buildlogic.libs")
}

repositories {  // TODO: Remove this once piston is published to enginehub repo
    mavenLocal()
}
dependencies {
    // These are here because they use net.kyori:text-api -- so they need to be relocated too
    "shade"(libs.piston.coreAp.annotations)
    "shade"(libs.piston.coreAp.processor)
    // These are here because they use net.kyori:adventure -- so they need to be relocated too
    "shade"("org.enginehub.piston.core-ap:annotations:${Versions.PISTON}")
    "shade"("org.enginehub.piston.core-ap:processor:${Versions.PISTON}")
}
