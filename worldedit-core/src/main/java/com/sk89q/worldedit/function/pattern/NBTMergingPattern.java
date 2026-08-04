/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.pattern;

import com.google.common.annotations.VisibleForTesting;
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

    @VisibleForTesting
    public Map<String, ? extends LinTag<?>> getNbtToMerge() {
        return nbtToMerge;
    }
}
