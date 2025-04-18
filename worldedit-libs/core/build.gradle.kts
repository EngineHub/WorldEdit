plugins {
    id("buildlogic.libs")
}

repositories { // TODO: Remove this once piston is published to enginehub repo
    mavenCentral()
    mavenLocal()
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {

    // legacy piston
    "shade"(libs.adventureText.api)
    "shade"(libs.adventureText.serializer.gson)
    "shade"(libs.adventureText.serializer.legacy)
    "shade"(libs.adventureText.serializer.plain)
    // These are here because they use net.kyori:text-api -- so they need to be relocated too
    "shade"(libs.piston.core)
    "shade"(libs.piston.coreAp.runtime)
    "shade"(libs.piston.defaultImpl)
    // legacy piston
    "shade"(libs.kyoriText.api)
    "shade"(libs.kyoriText.serializer.gson)
    "shade"(libs.kyoriText.serializer.legacy)
    "shade"(libs.kyoriText.serializer.plain)
    // Linbus
    "shade"(platform(libs.linBus.bom))
    "shade"(libs.linBus.common)
    "shade"(libs.linBus.stream)
    "shade"(libs.linBus.tree)
    "shade"(libs.linBus.format.snbt)
}
