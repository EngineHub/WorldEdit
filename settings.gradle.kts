rootProject.name = "worldedit"

include("worldedit-libs")

listOf("legacy", "1.17.1", "1.18.2", "1.19.4", "1.20", "1.20.2").forEach {
    include("worldedit-bukkit:adapters:adapter-$it")
}

listOf("bukkit", "core", "sponge", "fabric", "forge", "cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-mod")
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")
