plugins {
    id("buildlogic.libs")
}


repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {

    // legacy piston
    "shade"(libs.adventureText.serializer.ansi)
}
