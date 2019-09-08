applyLibrariesConfiguration()

dependencies {
    "shade"("net.kyori:text-api:${Versions.TEXT}")
    "shade"("net.kyori:text-serializer-gson:${Versions.TEXT}")
    "shade"("net.kyori:text-serializer-legacy:${Versions.TEXT}")
    "shade"("net.kyori:text-serializer-plain:${Versions.TEXT}")
    "shade"("com.sk89q:jchronic:0.2.4a") {
        exclude(group = "junit", module = "junit")
    }
    "shade"("com.thoughtworks.paranamer:paranamer:2.6")
    "shade"("com.sk89q.lib:jlibnoise:1.0.0")
    "shade"("org.enginehub.piston:core:${Versions.PISTON}")
    "shade"("org.enginehub.piston.core-ap:runtime:${Versions.PISTON}")
    "shade"("org.enginehub.piston:default-impl:${Versions.PISTON}")
}
