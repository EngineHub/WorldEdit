applyLibrariesConfiguration()
constrainDependenciesToLibsCore()

repositories {
    maven {
        name = "SpigotMC"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    "shade"("net.kyori:adventure-platform-bukkit:${Versions.ADVENTURE_EXTRAS}")
}
