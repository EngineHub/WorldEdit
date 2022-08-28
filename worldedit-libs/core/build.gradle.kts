applyLibrariesConfiguration()

dependencies {
    "shade"("net.kyori:text-api:${Versions.TEXT}")
    "shade"("net.kyori:text-serializer-gson:${Versions.TEXT}")
    "shade"("net.kyori:text-serializer-legacy:${Versions.TEXT}")
    "shade"("net.kyori:text-serializer-plain:${Versions.TEXT}")
    // These are here because they use net.kyori:text-api -- so they need to be relocated too
    "shade"("org.enginehub.piston:core:${Versions.PISTON}")
    "shade"("org.enginehub.piston.core-ap:runtime:${Versions.PISTON}")
    "shade"("org.enginehub.piston:default-impl:${Versions.PISTON}")
    // Linbus
    "shade"("org.enginehub.lin-bus:lin-bus-common:${Versions.LIN_BUS}")
    "shade"("org.enginehub.lin-bus:lin-bus-stream:${Versions.LIN_BUS}")
    "shade"("org.enginehub.lin-bus:lin-bus-tree:${Versions.LIN_BUS}")
    "shade"("org.enginehub.lin-bus.format:lin-bus-format-snbt:${Versions.LIN_BUS}")
}
