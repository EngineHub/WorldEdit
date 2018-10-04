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

package com.sk89q.worldedit.bukkit;

/**
 * Adds methods to test if different API methods are possible based on implementation.
 */
public class BukkitImplementationTester {

    private BukkitImplementationTester() {
    }

    /**
     * Known Bukkit implementations
     */
    public enum BukkitImplementation {
        CRAFTBUKKIT,
        SPIGOT,
        PAPER,
    }

    private static final String implementationMessage = "************************************************" +
                                                        "* Note: PaperMC (https://papermc.io/) is       *" +
                                                        "* recommended for optimal performance with     *" +
                                                        "* WorldEdit, WorldGuard, or CraftBook.         *" +
                                                        "************************************************";

    private static BukkitImplementation implementation;

    /**
     * Gets the implementation currently in use on the server.
     *
     * @return The server implementation
     */
    public static BukkitImplementation getImplementation() {
        if (implementation == null) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                implementation = BukkitImplementation.PAPER;
            } catch (Exception e) {
                try {
                    Class.forName("org.spigotmc.SpigotConfig");
                    implementation = BukkitImplementation.SPIGOT;
                } catch (Exception e2) {
                    implementation = BukkitImplementation.CRAFTBUKKIT;
                }
            }

            if (implementation != BukkitImplementation.PAPER) {
//                 Bukkit.getServer().getConsoleSender().sendMessage(implementationMessage); // TODO Decide if good idea.
            }
        }

        return implementation;
    }

    /**
     * Check if this implementation is compatible with Spigot APIs
     *
     * @return If compatible with Spigot APIs
     */
    public static boolean isSpigotCompatible() {
        return getImplementation() == BukkitImplementation.SPIGOT || getImplementation() == BukkitImplementation.PAPER;
    }

    /**
     * Check if this implementation is compatible with Paper APIs
     *
     * @return If compatible with Paper APIs
     */
    public static boolean isPaperCompatible() {
        return getImplementation() == BukkitImplementation.PAPER;
    }
}
