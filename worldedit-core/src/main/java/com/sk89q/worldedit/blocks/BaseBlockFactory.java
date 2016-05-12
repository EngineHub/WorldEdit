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

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseBlockFactory {
    private HashMap<Integer, List<BaseBlock>> baseBlockCache = new HashMap<Integer, List<BaseBlock>>();

    /**
     * Construct a block with the given ID and a data value of 0.
     *
     * @param id ID value
     */
    public BaseBlock getBaseBlock(int id) {
        return getBaseBlock(id, 0, null);
    }

    /**
     * Construct a block with the given ID and data value.
     *
     * @param id ID value
     * @param data data value
     */
    public BaseBlock getBaseBlock(int id, int data) {
        return getBaseBlock(id, data, null);
    }

    /**
     * Construct a block with the given ID, data value and NBT data structure.
     *
     * @param id ID value
     * @param data data value
     * @param nbtData NBT data, which may be null
     */
    public BaseBlock getBaseBlock(int id, int data, @Nullable CompoundTag nbtData) {
        if (nbtData == null) {
            return getCachedValue(id, data);
        }
        return new BaseBlock(id, data, nbtData);

    }

    private BaseBlock getCachedValue(int id, int data) {
        List<BaseBlock> baseBlocks = baseBlockCache.get(id);
        if (baseBlocks == null) {
            baseBlocks = new ArrayList<BaseBlock>();
            baseBlockCache.put(id, baseBlocks);
        }

        for (BaseBlock baseBlock : baseBlocks) {
            if (baseBlock.getType() == id && baseBlock.getData() == data) {
                return baseBlock;
            }
        }

        BaseBlock baseBlock = new BaseBlock(id, data, null);
        baseBlocks.add(baseBlock);
        return baseBlock;
    }
}
