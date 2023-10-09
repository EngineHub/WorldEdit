applyLibrariesConfiguration()

repositories {  // TODO: Remove this once piston is published to enginehub repo
    mavenLocal()
}
dependencies {
    // These are here because they use net.kyori:adventure -- so they need to be relocated too
    "shade"("org.enginehub.piston.core-ap:annotations:${Versions.PISTON}")
    "shade"("org.enginehub.piston.core-ap:processor:${Versions.PISTON}")
}
