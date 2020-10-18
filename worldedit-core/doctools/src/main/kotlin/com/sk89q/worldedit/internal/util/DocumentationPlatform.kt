package com.sk89q.worldedit.internal.util

import com.sk89q.worldedit.entity.Player
import com.sk89q.worldedit.extension.platform.AbstractPlatform
import com.sk89q.worldedit.world.World
import org.enginehub.piston.CommandManager
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties

class DocumentationPlatform : AbstractPlatform() {

    override fun getPlatformName() = "Documentation"

    override fun getVersion(): String {
        val props = Files.newBufferedReader(Paths.get("./gradle.properties")).use { reader ->
            Properties().also { it.load(reader) }
        }

        return props.getProperty("version") ?: "No version property in `gradle.properties`"
    }

    override fun getConfiguration() = DocumentationConfiguration

    override fun getPlatformVersion() = version

    override fun getRegistries() = error("Documentation does not provide this")

    override fun getDataVersion() = error("Documentation does not provide this")

    override fun isValidMobType(type: String?) = error("Documentation does not provide this")

    override fun matchPlayer(player: Player?) = error("Documentation does not provide this")

    override fun matchWorld(world: World?) = error("Documentation does not provide this")

    override fun registerCommands(commandManager: CommandManager?) = error("Documentation does not provide this")

    override fun registerGameHooks() = error("Documentation does not provide this")

    override fun getCapabilities() = error("Documentation does not provide this")

    override fun getSupportedSideEffects() = error("Documentation does not provide this")

}
