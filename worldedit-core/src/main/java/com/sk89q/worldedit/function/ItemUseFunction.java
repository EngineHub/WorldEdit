package com.sk89q.worldedit.function;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.World;

public final class ItemUseFunction implements RegionFunction {
    private final World world;
    private final BaseItem item;

    public ItemUseFunction(World world, BaseItem item) {
        this.world = world;
        this.item = item;
    }

    @Override
    public boolean apply(BlockVector3 position) throws WorldEditException {
        return world.useItem(position, item, Direction.UP);
    }
}
