plugins {
    id("buildlogic.libs")
}

dependencies {
    "shade"(libs.kyoriText.api)
    "shade"(libs.kyoriText.serializer.gson)
    "shade"(libs.kyoriText.serializer.legacy)
    "shade"(libs.kyoriText.serializer.plain)
    // These are here because they use net.kyori:text-api -- so they need to be relocated too
    "shade"(libs.piston.core)
    "shade"(libs.piston.coreAp.runtime)
    "shade"(libs.piston.defaultImpl)
}
