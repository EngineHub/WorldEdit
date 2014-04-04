package com.sk89q.worldedit.forge.selections;

import net.minecraft.world.World;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.RegionSelector;

public abstract interface Selection {
    public abstract Location getMinimumPoint();

    public abstract Vector getNativeMinimumPoint();

    public abstract Location getMaximumPoint();

    public abstract Vector getNativeMaximumPoint();

    public abstract RegionSelector getRegionSelector();

    public abstract World getWorld();

    public abstract int getArea();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract int getLength();

    public abstract boolean contains(Location paramLocation);
}