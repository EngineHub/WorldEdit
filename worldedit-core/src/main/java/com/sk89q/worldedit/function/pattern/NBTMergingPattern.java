package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

import java.util.Map;

public class NBTMergingPattern extends AbstractExtentPattern {
    private final Map<String, ? extends LinTag<?>> nbtToMerge;

    public NBTMergingPattern(Extent extent, Map<String, ? extends LinTag<?>> nbtToMerge) {
        super(extent);
        this.nbtToMerge = nbtToMerge;
    }

    @Override
    public BaseBlock applyBlock(BlockVector3 position) {
        BaseBlock baseBlock = getExtent().getFullBlock(position);
        LinCompoundTag.Builder nbtBuilder;
        if (baseBlock.getNbt() != null) {
            nbtBuilder = baseBlock.getNbt().toBuilder();
        } else {
            nbtBuilder = LinCompoundTag.builder();
        }
        nbtBuilder.putAll(nbtToMerge);
        return baseBlock.toBaseBlock(nbtBuilder.build());
    }
}
