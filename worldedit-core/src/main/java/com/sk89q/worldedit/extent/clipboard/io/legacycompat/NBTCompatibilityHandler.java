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

package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

import java.util.HashMap;
import java.util.Map;

public interface NBTCompatibilityHandler {
    /**
     * Check if this is a block affected by this handler.
     *
     * @deprecated this is handled by {@link #updateNbt(BaseBlock)} now
     */
    @Deprecated
    default <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
        BaseBlock state = block.toBaseBlock();
        BaseBlock updated = updateNbt(state);
        return state != updated;
    }

    @Deprecated
    default <B extends BlockStateHolder<B>> BlockStateHolder<?> updateNBT(B block, Map<String, Tag<?, ?>> values) {
        BaseBlock changed = updateNbt(block.toBaseBlock(LazyReference.from(() -> {
            var builder = LinCompoundTag.builder();
            for (var entry : values.entrySet()) {
                builder.put(entry.getKey(), entry.getValue().toLinTag());
            }
            return builder.build();
        })));
        CompoundTag data = changed.getNbtData();
        values.clear();
        if (data != null) {
            values.putAll(data.getValue());
        }
        return changed;
    }

    /**
     * Given a block, update the block's NBT. The NBT may be {@code null}.
     *
     * @param block the block to update
     * @return the updated block, or the same block if no change is necessary
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "updateNBT",
        delegateParams = { BlockStateHolder.class, Map.class }
    )
    default BaseBlock updateNbt(BaseBlock block) {
        DeprecationUtil.checkDelegatingOverride(getClass());
        if (!isAffectedBlock(block)) {
            return block;
        }
        if (block.getNbt() == null) {
            return block;
        }
        @SuppressWarnings("deprecation")
        Map<String, Tag<?, ?>> values = new HashMap<>(new CompoundTag(block.getNbt()).getValue());
        BlockStateHolder<?> changedBlock = updateNBT(block, values);
        return changedBlock.toBaseBlock(LazyReference.from(() -> {
            var builder = LinCompoundTag.builder();
            for (@SuppressWarnings("deprecation") var entry : values.entrySet()) {
                LinTag<?> linTag = entry.getValue().toLinTag();
                builder.put(entry.getKey(), linTag);
            }
            return builder.build();
        }));
    }

}
