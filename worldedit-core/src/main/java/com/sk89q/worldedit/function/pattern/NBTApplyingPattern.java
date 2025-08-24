package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.enginehub.linbus.tree.LinCompoundTag;

public class NBTApplyingPattern extends AbstractExtentPattern {
    private final LinCompoundTag nbtToSet;

    public NBTApplyingPattern(Extent extent, LinCompoundTag nbtToSet) {
        super(extent);
        this.nbtToSet = nbtToSet;
    }

    @Override
    public BaseBlock applyBlock(BlockVector3 position) {
        BlockState block = getExtent().getBlock(position);
        return block.toBaseBlock(nbtToSet);
    }
}
