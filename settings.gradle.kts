rootProject.name = "worldedit"

include("worldedit-libs")

listOf("bukkit", "core", "forge", "sponge", "fabric").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")
