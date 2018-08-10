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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BundledBlockRegistry;
import com.sk89q.worldedit.world.registry.PassthroughBlockMaterial;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

public class BukkitBlockRegistry extends BundledBlockRegistry {

    private Map<Material, BukkitBlockMaterial> materialMap = new EnumMap<>(Material.class);

    @Nullable
    @Override
    public BlockMaterial getMaterial(BlockType blockType) {
        return materialMap.computeIfAbsent(BukkitAdapter.adapt(blockType),
                material -> new BukkitBlockMaterial(BukkitBlockRegistry.super.getMaterial(blockType), material));
    }

    @Nullable
    @Override
    public Map<String, ? extends Property> getProperties(BlockType blockType) {
        if (WorldEditPlugin.getInstance().getBukkitImplAdapter() != null) {
            return WorldEditPlugin.getInstance().getBukkitImplAdapter().getProperties(blockType);
        }
        return super.getProperties(blockType);
    }

    public static class BukkitBlockMaterial extends PassthroughBlockMaterial {

        private final Material material;

        public BukkitBlockMaterial(@Nullable BlockMaterial material, Material bukkitMaterial) {
            super(material);
            this.material = bukkitMaterial;
        }

        @Override
        public boolean isSolid() {
            return material.isSolid();
        }

        @Override
        public boolean isBurnable() {
            return material.isBurnable();
        }

        @Override
        public boolean isTranslucent() {
            return material.isTransparent();
        }
    }
}
