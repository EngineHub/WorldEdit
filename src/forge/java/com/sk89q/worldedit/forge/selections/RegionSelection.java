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

package com.sk89q.worldedit.forge.selections;

import java.lang.ref.WeakReference;

import net.minecraft.world.World;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.forge.WorldEditMod;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;

public abstract class RegionSelection implements Selection {
    private WeakReference<World> world;
    private RegionSelector selector;
    private Region region;

    public RegionSelection(World world) {
        this.world = new WeakReference<World>(world);
    }

    public RegionSelection(World world, RegionSelector selector, Region region) {
        this(world);
        this.region = region;
        this.selector = selector;
    }

    protected Region getRegion() {
        return this.region;
    }

    protected void setRegion(Region region) {
        this.region = region;
    }

    public RegionSelector getRegionSelector() {
        return this.selector;
    }

    protected void setRegionSelector(RegionSelector selector) {
        this.selector = selector;
    }

    public Location getMinimumPoint() {
        return new Location(WorldEditMod.inst.getWorld(this.world.get()), this.region.getMinimumPoint());
    }

    public Vector getNativeMinimumPoint() {
        return this.region.getMinimumPoint();
    }

    public Location getMaximumPoint() {
        return new Location(WorldEditMod.inst.getWorld(this.world.get()), this.region.getMaximumPoint());
    }

    public Vector getNativeMaximumPoint() {
        return this.region.getMaximumPoint();
    }

    public World getWorld() {
        return this.world.get();
    }

    public int getArea() {
        return this.region.getArea();
    }

    public int getWidth() {
        return this.region.getWidth();
    }

    public int getHeight() {
        return this.region.getHeight();
    }

    public int getLength() {
        return this.region.getLength();
    }

    public boolean contains(Location pt) {
        if (!pt.getWorld().equals(this.world.get())) {
            return false;
        }

        return this.region.contains(new Vector(pt.getPosition()));
    }
}