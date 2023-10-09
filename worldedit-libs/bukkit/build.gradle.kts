plugins {
    id("buildlogic.libs")
}

dependencies {
    "shade"(libs.kyoriText.adapter.bukkit)
    "shade"("net.kyori:adventure-platform-bukkit:${Versions.KYORI_PLATFORM_BUKKIT}")
}
