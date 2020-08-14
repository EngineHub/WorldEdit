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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.Collections;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;

/**
 * A block registry that uses {@link BundledBlockData} to serve information
 * about blocks.
 */
public class BundledBlockRegistry implements BlockRegistry {

    @Override
    public Component getRichName(BlockType blockType) {
        BundledBlockData.BlockEntry blockEntry = BundledBlockData.getInstance().findById(blockType.getId());
        if (blockEntry != null) {
            // This is more likely to be "right", but not translated
            // Some vanilla MC blocks have overrides so we need this name here
            // Most platforms should be overriding this anyways, so it likely doesn't matter
            // too much!
            return TextComponent.of(blockEntry.localizedName);
        }
        return TranslatableComponent.of(
            TranslationManager.makeTranslationKey("block", blockType.getId())
        );
    }

    @Nullable
    @Override
    @Deprecated
    // dumb_intellij.jpg
    @SuppressWarnings("deprecation")
    public String getName(BlockType blockType) {
        BundledBlockData.BlockEntry blockEntry = BundledBlockData.getInstance().findById(blockType.getId());
        return blockEntry != null ? blockEntry.localizedName : null;
    }

    @Nullable
    @Override
    public BlockMaterial getMaterial(BlockType blockType) {
        return new PassthroughBlockMaterial(BundledBlockData.getInstance().getMaterialById(blockType.getId()));
    }

    @Nullable
    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        return Collections.emptyMap(); // Oof
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        return OptionalInt.empty();
    }

}
