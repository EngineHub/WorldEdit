plugins {
    id("buildlogic.libs")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    "shade"(libs.adventureText.adapter.bukkit)
}
