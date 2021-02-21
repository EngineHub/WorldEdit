applyLibrariesConfiguration()
constrainDependenciesToLibsCore()

repositories {
    maven {
        name = "Sponge"
        url = uri("https://repo-new.spongepowered.org/maven")
    }
    maven {
        name = "Sponge-Old"
        url = uri("https://repo.spongepowered.org/maven")
    }
}
dependencies {
    "shade"("net.kyori:text-adapter-spongeapi:${Versions.TEXT_EXTRAS}")
}
