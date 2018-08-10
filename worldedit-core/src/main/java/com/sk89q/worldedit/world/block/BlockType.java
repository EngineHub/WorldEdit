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

package com.sk89q.worldedit.world.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

public class BlockType {

    public static final NamespacedRegistry<BlockType> REGISTRY = new NamespacedRegistry<>("block type");

    private String id;
    private BlockState defaultState;
    private Map<String, ? extends Property> properties;
    private BlockMaterial blockMaterial;

    public BlockType(String id) {
        this(id, null);
    }

    public BlockType(String id, Function<BlockState, BlockState> values) {
        // If it has no namespace, assume minecraft.
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        this.id = id;
        this.defaultState = new ArrayList<>(BlockState.generateStateMap(this).values()).get(0);
        if (values != null) {
            this.defaultState = values.apply(this.defaultState);
        }
    }

    /**
     * Gets the ID of this block.
     *
     * @return The id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the name of this block, or the ID if the name cannot be found.
     *
     * @return The name, or ID
     */
    public String getName() {
        BundledBlockData.BlockEntry entry = BundledBlockData.getInstance().findById(this.id);
        if (entry == null) {
            return getId();
        } else {
            return entry.localizedName;
        }
    }

    /**
     * Gets the properties of this BlockType in a key->property mapping.
     *
     * @return The properties map
     */
    public Map<String, ? extends Property> getPropertyMap() {
        if (properties == null) {
            properties = ImmutableMap.copyOf(WorldEdit.getInstance().getPlatformManager()
                    .queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getProperties(this));
        }
        return this.properties;
    }

    /**
     * Gets the properties of this BlockType.
     *
     * @return the properties
     */
    public List<? extends Property> getProperties() {
        return ImmutableList.copyOf(this.getPropertyMap().values());
    }

    /**
     * Gets a property by name.
     *
     * @param name The name
     * @return The property
     */
    public <V> Property<V> getProperty(String name) {
        return getPropertyMap().get(name);
    }

    /**
     * Gets the default state of this block type.
     *
     * @return The default state
     */
    public BlockState getDefaultState() {
        return this.defaultState;
    }

    /**
     * Gets whether this block type has an item representation.
     *
     * @return If it has an item
     */
    public boolean hasItemType() {
        return getItemType() != null;
    }

    /**
     * Gets the item representation of this block type, if it exists.
     *
     * @return The item representation
     */
    @Nullable
    public ItemType getItemType() {
        return ItemTypes.get(this.id);
    }

    /**
     * Get the material for this BlockType.
     *
     * @return The material
     */
    public BlockMaterial getMaterial() {
        if (this.blockMaterial == null) {
            this.blockMaterial = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getMaterial(this);
        }
        return this.blockMaterial;
    }

    /**
     * Gets the legacy ID. Needed for legacy reasons.
     *
     * DO NOT USE THIS.
     *
     * @return legacy id or 0, if unknown
     */
    @Deprecated
    public int getLegacyId() {
        int[] id = LegacyMapper.getInstance().getLegacyFromBlock(this.getDefaultState());
        if (id != null) {
            return id[0];
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockType && this.id.equals(((BlockType) obj).id);
    }
}
