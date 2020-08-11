applyLibrariesConfiguration()

dependencies {
    "shade"("net.kyori:adventure-api:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-gson:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-legacy:${Versions.ADVENTURE}")
    "shade"("net.kyori:adventure-text-serializer-plain:${Versions.ADVENTURE}")
    "shade"("com.sk89q:jchronic:0.2.4a") {
        exclude(group = "junit", module = "junit")
    }
    "shade"("com.thoughtworks.paranamer:paranamer:2.6")
    "shade"("com.sk89q.lib:jlibnoise:1.0.0")
    "shade"("org.enginehub.piston:core:${Versions.PISTON}")
    "shade"("org.enginehub.piston.core-ap:runtime:${Versions.PISTON}")
    "shade"("org.enginehub.piston:default-impl:${Versions.PISTON}")
}
