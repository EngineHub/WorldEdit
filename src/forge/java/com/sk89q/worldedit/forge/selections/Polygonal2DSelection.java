package com.sk89q.worldedit.forge.selections;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.World;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.forge.WorldEditMod;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;

public class Polygonal2DSelection extends RegionSelection {
    protected Polygonal2DRegion poly2d;

    public Polygonal2DSelection(World world, RegionSelector sel, Polygonal2DRegion region) {
        super(world, sel, region);
        this.poly2d = region;
    }

    public Polygonal2DSelection(World world, List points, int minY, int maxY) {
        super(world);
        LocalWorld lWorld = WorldEditMod.inst.getWorld(world);

        minY = Math.min(Math.max(0, minY), world.getActualHeight());
        maxY = Math.min(Math.max(0, maxY), world.getActualHeight());

        Polygonal2DRegionSelector sel = new Polygonal2DRegionSelector(lWorld, points, minY, maxY);

        this.poly2d = sel.getIncompleteRegion();

        setRegionSelector(sel);
        setRegion(this.poly2d);
    }

    public List getNativePoints() {
        return Collections.unmodifiableList(this.poly2d.getPoints());
    }
}