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

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.enginehub.linbus.tree.LinCompoundTag;

import javax.annotation.Nullable;

@Deprecated
public class LegacyBaseBlockWrapper extends BaseBlock {
    protected LegacyBaseBlockWrapper(BlockState blockState) {
        super(blockState);
    }

    // These two methods force the legacy blocks to use the old NBT methods.
    @Nullable
    @Override
    public LazyReference<LinCompoundTag> getNbtReference() {
        CompoundTag nbtData = getNbtData();
        return nbtData == null ? null : LazyReference.from(nbtData::toLinTag);
    }

    @Override
    public void setNbtReference(@Nullable LazyReference<LinCompoundTag> nbtData) {
        setNbtData(nbtData == null ? null : new CompoundTag(nbtData.getValue()));
    }
}
