applyLibrariesConfiguration()

dependencies {
    // These are here because they use net.kyori:text-api -- so they need to be relocated too
    "shade"("org.enginehub.piston.core-ap:annotations:${Versions.PISTON}")
    "shade"("org.enginehub.piston.core-ap:processor:${Versions.PISTON}")
}
