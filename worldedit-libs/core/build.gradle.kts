applyLibrariesConfiguration()

repositories { // TODO: Remove this once piston is published to enginehub repo
    mavenLocal()
}

dependencies {
    "shade"("net.kyori:adventure-api:${Versions.KYORI_ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-gson:${Versions.KYORI_ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-legacy:${Versions.KYORI_ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-plain:${Versions.KYORI_ADVENTURE}")
    // These are here because they use net.kyori:adventure -- so they need to be relocated too
    "shade"("org.enginehub.piston:core:${Versions.PISTON}")
    "shade"("org.enginehub.piston.core-ap:runtime:${Versions.PISTON}")
    "shade"("org.enginehub.piston:default-impl:${Versions.PISTON}")
    // Linbus
    "shade"("org.enginehub.lin-bus:lin-bus-common:${Versions.LIN_BUS}")
    "shade"("org.enginehub.lin-bus:lin-bus-stream:${Versions.LIN_BUS}")
    "shade"("org.enginehub.lin-bus:lin-bus-tree:${Versions.LIN_BUS}")
    "shade"("org.enginehub.lin-bus.format:lin-bus-format-snbt:${Versions.LIN_BUS}")
}
