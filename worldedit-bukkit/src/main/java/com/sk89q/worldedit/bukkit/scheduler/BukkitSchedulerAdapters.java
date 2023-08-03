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
