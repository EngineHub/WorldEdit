plugins {
    id("buildlogic.libs")
}

repositories {  // TODO: Remove this once piston is published to enginehub repo
    mavenCentral()
    mavenLocal()
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
}
dependencies {
    // These are here because they use net.kyori:text-api -- so they need to be relocated too
    "shade"(libs.piston.coreAp.annotations)
    "shade"(libs.piston.coreAp.processor)
}
