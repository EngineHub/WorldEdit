package com.sk89q.worldedit.forge.selections;

import net.minecraft.world.World;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.forge.WorldEditMod;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;

public class CuboidSelection extends RegionSelection {
    protected CuboidRegion cuboid;

    public CuboidSelection(World world, Location pt1, Location pt2) {
        this(world, pt1 == null ? null : pt1.getPosition(), pt2 == null ? null : pt2.getPosition());
    }

    public CuboidSelection(World world, Vector pt1, Vector pt2) {
        super(world);

        if (pt1 == null) {
            throw new IllegalArgumentException("Null point 1 not permitted");
        }

        if (pt2 == null) {
            throw new IllegalArgumentException("Null point 2 not permitted");
        }

        CuboidRegionSelector sel = new CuboidRegionSelector(WorldEditMod.inst.getWorld(world));

        sel.selectPrimary(pt1);
        sel.selectSecondary(pt2);

        this.cuboid = sel.getIncompleteRegion();

        setRegionSelector(sel);
        setRegion(this.cuboid);
    }

    public CuboidSelection(World world, RegionSelector sel, CuboidRegion region) {
        super(world, sel, region);
        this.cuboid = region;
    }
}