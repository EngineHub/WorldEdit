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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.PassthroughBlockMaterial;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;

public class BukkitBlockRegistry implements BlockRegistry {
    private final Map<Material, BukkitBlockMaterial> materialMap = new HashMap<>();

    @Override
    public Component getRichName(BlockType blockType) {
        if (WorldEditPlugin.getInstance().getBukkitImplAdapter() != null) {
            return WorldEditPlugin.getInstance().getBukkitImplAdapter().getRichBlockName(blockType);
        }
        return TextComponent.of(blockType.id());
    }

    @Nullable
    @Override
    public BlockMaterial getMaterial(BlockType blockType) {
        Material mat = BukkitAdapter.adapt(blockType);
        if (mat == null) {
            return null;
        }
        return materialMap.computeIfAbsent(mat, material -> {
            BlockMaterial platformMaterial = null;
            if (WorldEditPlugin.getInstance().getBukkitImplAdapter() != null) {
                platformMaterial = WorldEditPlugin.getInstance().getBukkitImplAdapter().getBlockMaterial(blockType);
            }
            return new BukkitBlockMaterial(platformMaterial, material);
        });
    }

    @Nullable
    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        if (WorldEditPlugin.getInstance().getBukkitImplAdapter() != null) {
            return WorldEditPlugin.getInstance().getBukkitImplAdapter().getProperties(blockType);
        }
        return null;
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        if (WorldEditPlugin.getInstance().getBukkitImplAdapter() != null) {
            return WorldEditPlugin.getInstance().getBukkitImplAdapter().getInternalBlockStateId(state);
        }
        return OptionalInt.empty();
    }

    public static class BukkitBlockMaterial extends PassthroughBlockMaterial {

        private final Material material;

        public BukkitBlockMaterial(@Nullable BlockMaterial material, Material bukkitMaterial) {
            super(material);
            this.material = bukkitMaterial;
        }

        @Override
        public boolean isAir() {
            return material.isAir();
        }

        @Override
        public boolean isSolid() {
            return material.isSolid();
        }

        @Override
        public boolean isBurnable() {
            return material.isBurnable();
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean isTranslucent() {
            return super.isTranslucent() || material.isTransparent();
        }
    }
}
