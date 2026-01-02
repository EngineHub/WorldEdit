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

package com.sk89q.worldedit.bukkit.scheduler;

import com.sk89q.worldedit.bukkit.scheduler.adapters.BukkitSchedulerAdapter;
import com.sk89q.worldedit.bukkit.scheduler.adapters.FoliaSchedulerAdapter;
import com.sk89q.worldedit.extension.platform.scheduler.SchedulerAdapter;
import org.bukkit.plugin.Plugin;

public final class BukkitSchedulerAdapters {
    private final static boolean FOLIA_SUPPORT = foliaSupport();

    private BukkitSchedulerAdapters() {
        // Call only through a method
    }

    public static SchedulerAdapter create(Plugin plugin) {
        if (FOLIA_SUPPORT) {
            return new FoliaSchedulerAdapter(plugin);
        }
        return new BukkitSchedulerAdapter(plugin);
    }

    private static boolean foliaSupport() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
