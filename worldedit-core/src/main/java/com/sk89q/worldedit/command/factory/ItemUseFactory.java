package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.function.ItemUseFunction;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.world.World;

public final class ItemUseFactory implements Contextual<RegionFunction> {
    private final BaseItem item;

    public ItemUseFactory(BaseItem item) {
        this.item = item;
    }

    @Override
    public RegionFunction createFromContext(EditContext input) {
        World world = ((EditSession) input.getDestination()).getWorld();
        return new ItemUseFunction(world, item);
    }

    @Override
    public String toString() {
        return "application of the item " + item.getType() + ":" + item.getNbtData();
    }
}
