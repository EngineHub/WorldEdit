rootProject.name = "worldedit"

include("worldedit-libs")

// Forge has been removed until FG 5 is available.
listOf("bukkit", "core", "sponge", "fabric", "cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-mod")
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")
