rootProject.name = "worldedit"

include("worldedit-libs")

include("worldedit-bukkit:adapters:adapter-1.17.1")
include("worldedit-bukkit:adapters:adapter-1.18")

listOf("bukkit", "core", "sponge", "fabric", "forge", "cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-mod")
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")
