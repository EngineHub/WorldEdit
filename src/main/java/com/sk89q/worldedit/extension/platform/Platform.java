/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extension.platform;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.world.World;

import java.util.List;

/**
 * Represents a platform that WorldEdit has been implemented for.
 * </p>
 * It is strongly recommended that implementations extend from
 * {@link AbstractPlatform}.
 */
public interface Platform {

    /**
     * Resolves an item name to its ID.
     *
     * @param name The name to look up
     * @return The id that corresponds to the name, or -1 if no such ID exists
     */
    int resolveItem(String name);

    /**
     * Checks if a mob type is valid.
     *
     * @param type The mob type name to check
     * @return Whether the name is a valid mod bype
     */
    boolean isValidMobType(String type);

    /**
     * Reload WorldEdit configuration.
     */
    void reload();

    /**
     * Returns all available biomes.
     *
     * @return an object containing all the biomes
     */
    BiomeTypes getBiomes();

    /**
     * Schedules the given <code>task</code> to be invoked once every <code>period</code> ticks
     * after an initial delay of <code>delay</code> ticks.
     *
     * @param delay Delay in server ticks before executing first repeat
     * @param period Period in server ticks of the task
     * @param task Task to be executed
     * @return Task id number (-1 if scheduling failed)
     */
    int schedule(long delay, long period, Runnable task);

    List<? extends World> getWorlds();

    @Deprecated
    void onCommandRegistration(List<Command> commands);

    void onCommandRegistration(List<Command> commands, CommandsManager<LocalPlayer> manager);

    /**
     * Get the configuration from this platform.
     *
     * @return the configuration
     */
    LocalConfiguration getConfiguration();

    /**
     * Get the version of WorldEdit that this platform provides.
     * </p>
     * This version should match WorldEdit releases because it may be
     * checked to match.
     *
     * @return the version
     */
    String getVersion();

    /**
     * Get a friendly name of the platform.
     * </p>
     * The name can be anything (reasonable). An example name may be
     * "Bukkit" or "Forge".
     *
     * @return the platform name
     */
    String getPlatformName();

    /**
     * Get the version of the platform, which can be anything.
     *
     * @return the platform version
     */
    String getPlatformVersion();

}
