rootProject.name = "worldedit"

include("worldedit-libs")

listOf("1.17.1", "1.18.2", "1.19", "1.19.3", "1.19.4").forEach {
    include("worldedit-bukkit:adapters:adapter-$it")
}

listOf("bukkit", "core", "sponge", "fabric", "forge", "cli").forEach {
    include("worldedit-libs:$it")
    include("worldedit-$it")
}
include("worldedit-mod")
include("worldedit-libs:core:ap")

include("worldedit-core:doctools")

include("verification")
