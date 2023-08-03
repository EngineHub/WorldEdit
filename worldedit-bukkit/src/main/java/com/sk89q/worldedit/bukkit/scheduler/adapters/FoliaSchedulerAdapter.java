package com.sk89q.worldedit.bukkit.scheduler.adapters;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.scheduler.SchedulerAdapter;
import com.sk89q.worldedit.util.Location;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaSchedulerAdapter implements SchedulerAdapter {
    private final Plugin plugin;
    private final AsyncScheduler asyncScheduler;
    private final RegionScheduler regionScheduler;

    public FoliaSchedulerAdapter(final Plugin plugin) {
        this.plugin = plugin;
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
    }

    @Override
    public void runAsyncRate(final Runnable runnable, final long delay, final long period) {
        asyncScheduler.runAtFixedRate(plugin, task -> runnable.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void executeAtRegion(final Location location, final Runnable runnable) {
        regionScheduler.execute(plugin, BukkitAdapter.adapt(location), runnable);
    }

    @Override
    public void cancelTasks() {
        asyncScheduler.cancelTasks(plugin);
    }

}
