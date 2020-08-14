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

package com.sk89q.worldedit.world.item;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.util.GuavaUtil;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.ItemMaterial;

import javax.annotation.Nullable;

public class ItemType implements Keyed {

    public static final NamespacedRegistry<ItemType> REGISTRY = new NamespacedRegistry<>("item type");

    private final String id;
    @SuppressWarnings("deprecation")
    private final LazyReference<String> name = LazyReference.from(() -> {
        String name = GuavaUtil.firstNonNull(
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS)
                .getRegistries().getItemRegistry().getName(this),
            ""
        );
        return name.isEmpty() ? getId() : name;
    });
    private final LazyReference<Component> richName = LazyReference.from(() ->
        WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS)
            .getRegistries().getItemRegistry().getRichName(this)
    );
    private final LazyReference<ItemMaterial> itemMaterial = LazyReference.from(() ->
        WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS)
            .getRegistries().getItemRegistry().getMaterial(this)
    );

    public ItemType(String id) {
        // If it has no namespace, assume minecraft.
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public Component getRichName() {
        return richName.getValue();
    }

    /**
     * Gets the name of this item, or the ID if the name cannot be found.
     *
     * @return The name, or ID
     * @deprecated Names are translatable now, use {@link #getRichName()}.
     */
    @Deprecated
    public String getName() {
        return name.getValue();
    }


    /**
     * Gets whether this item type has a block representation.
     *
     * @return If it has a block
     */
    public boolean hasBlockType() {
        return getBlockType() != null;
    }

    /**
     * Gets the block representation of this item type, if it exists.
     *
     * @return The block representation
     */
    @Nullable
    public BlockType getBlockType() {
        return BlockTypes.get(this.id);
    }

    /**
     * Get the material for this ItemType.
     *
     * @return The material
     */
    public ItemMaterial getMaterial() {
        return itemMaterial.getValue();
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemType && this.id.equals(((ItemType) obj).id);
    }
}
