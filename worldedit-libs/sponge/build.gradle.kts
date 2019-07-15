applyLibrariesConfiguration()

repositories {
    maven {
        name = "Sponge"
        url = uri("https://repo.spongepowered.org/maven")
    }
}
dependencies {
    "shade"("net.kyori:text-adapter-spongeapi:${Versions.TEXT_EXTRAS}")
}