/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.util

import com.sk89q.worldedit.entity.Player
import com.sk89q.worldedit.extension.platform.AbstractPlatform
import com.sk89q.worldedit.extension.platform.Capability
import com.sk89q.worldedit.extension.platform.Preference
import com.sk89q.worldedit.world.World
import org.enginehub.piston.CommandManager
import java.nio.file.Files
import java.nio.file.Paths
import java.util.EnumMap
import java.util.Properties

class DocumentationPlatform : AbstractPlatform() {

    override fun getPlatformName() = "Documentation"

    override fun getVersion(): String {
        val props = Files.newBufferedReader(Paths.get("./gradle.properties")).use { reader ->
            Properties().also { it.load(reader) }
        }

        return props.getProperty("version") ?: "No version property in `gradle.properties`"
    }

    override fun getId() = "enginehub:documentation"

    override fun getConfiguration() = DocumentationConfiguration

    override fun getPlatformVersion() = version

    override fun getRegistries() = error("Documentation does not provide this")

    override fun getDataVersion() = error("Documentation does not provide this")

    override fun isValidMobType(type: String?) = error("Documentation does not provide this")

    override fun matchPlayer(player: Player?) = error("Documentation does not provide this")

    override fun matchWorld(world: World?) = error("Documentation does not provide this")

    override fun registerCommands(commandManager: CommandManager?) {}

    override fun setGameHooksEnabled(enabled: Boolean) {}

    override fun getCapabilities(): MutableMap<Capability, Preference> {
        val capabilities: MutableMap<Capability, Preference> = EnumMap(Capability::class.java)
        capabilities[Capability.CONFIGURATION] = Preference.PREFER_OTHERS
        capabilities[Capability.WORLDEDIT_CUI] = Preference.PREFER_OTHERS
        capabilities[Capability.GAME_HOOKS] = Preference.PREFER_OTHERS
        capabilities[Capability.PERMISSIONS] = Preference.PREFER_OTHERS
        capabilities[Capability.USER_COMMANDS] = Preference.PREFER_OTHERS
        capabilities[Capability.WORLD_EDITING] = Preference.PREFER_OTHERS
        return capabilities
    }

    override fun getSupportedSideEffects() = error("Documentation does not provide this")

}
