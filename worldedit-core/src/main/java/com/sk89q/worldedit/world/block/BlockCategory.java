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

package com.sk89q.worldedit.world.block;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

import java.util.Set;

/**
 * A category of blocks. This is due to the splitting up of
 * blocks such as wool into separate ids.
 */
public class BlockCategory extends Category<BlockType> implements Keyed {

    public static final NamespacedRegistry<BlockCategory> REGISTRY = new NamespacedRegistry<>("block tag");

    public BlockCategory(final String id) {
        super(id);
    }

    @Override
    protected Set<BlockType> load() {
        return WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getRegistries()
                .getBlockCategoryRegistry().getAll(this);
    }

    /**
     * Checks whether the BlockStateHolder is contained within
     * this category.
     *
     * @param blockStateHolder The blockstateholder
     * @return If it's a part of this category
     */
    public <B extends BlockStateHolder<B>> boolean contains(B blockStateHolder) {
        return this.getAll().contains(blockStateHolder.getBlockType());
    }
}
