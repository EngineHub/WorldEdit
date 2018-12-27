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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class BlockType {

    public static final NamespacedRegistry<BlockType> REGISTRY = new NamespacedRegistry<>("block type");

    private final String id;
    private final Function<BlockState, BlockState> values;
    private final AtomicReference<BlockState> defaultState = new AtomicReference<>();
    private final AtomicReference<FuzzyBlockState> emptyFuzzy = new AtomicReference<>();
    private final AtomicReference<Map<String, ? extends Property<?>>> properties = new AtomicReference<>();
    private final AtomicReference<BlockMaterial> blockMaterial = new AtomicReference<>();
    private final AtomicReference<Map<Map<Property<?>, Object>, BlockState>> blockStatesMap = new AtomicReference<>();

    public BlockType(String id) {
        this(id, null);
    }

    public BlockType(String id, Function<BlockState, BlockState> values) {
        // If it has no namespace, assume minecraft.
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        this.id = id;
        this.values = values;
    }

    private <T> T updateField(AtomicReference<T> field, Supplier<T> value) {
        T result = field.get();
        if (result == null) {
            // swap in new value, if someone doesn't beat us
            T update = value.get();
            if (field.compareAndSet(null, update)) {
                // use ours
                result = update;
            } else {
                // update to real value
                result = field.get();
            }
        }
        return result;
    }

    private Map<Map<Property<?>, Object>, BlockState> getBlockStatesMap() {
        return updateField(blockStatesMap, () -> BlockState.generateStateMap(this));
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
        String name = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getName(this);
        if (name == null) {
            return getId();
        } else {
            return name;
        }
    }

    /**
     * Gets the properties of this BlockType in a {@code key->property} mapping.
     *
     * @return The properties map
     */
    public Map<String, ? extends Property<?>> getPropertyMap() {
        return updateField(properties, () -> ImmutableMap.copyOf(WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getProperties(this)));
    }

    /**
     * Gets the properties of this BlockType.
     *
     * @return the properties
     */
    public List<? extends Property<?>> getProperties() {
        return ImmutableList.copyOf(this.getPropertyMap().values());
    }

    /**
     * Gets a property by name.
     *
     * @param name The name
     * @return The property
     */
    public <V> Property<V> getProperty(String name) {
        // Assume it works, CCE later at runtime if not.
        @SuppressWarnings("unchecked")
        Property<V> property = (Property<V>) getPropertyMap().get(name);
        checkArgument(property != null, "%s has no property named %s", this, name);
        return property;
    }

    /**
     * Gets the default state of this block type.
     *
     * @return The default state
     */
    public BlockState getDefaultState() {
        return updateField(defaultState, () -> {
            BlockState defaultState = new ArrayList<>(getBlockStatesMap().values()).get(0);
            if (values != null) {
                defaultState = values.apply(defaultState);
            }
            return defaultState;
        });
    }

    public FuzzyBlockState getFuzzyMatcher() {
        return updateField(emptyFuzzy, () -> new FuzzyBlockState(this));
    }

    /**
     * Gets a list of all possible states for this BlockType.
     *
     * @return All possible states
     */
    public List<BlockState> getAllStates() {
        return ImmutableList.copyOf(getBlockStatesMap().values());
    }

    /**
     * Gets a state of this BlockType with the given properties.
     *
     * @return The state, if it exists
     */
    public BlockState getState(Map<Property<?>, Object> key) {
        BlockState state = getBlockStatesMap().get(key);
        checkArgument(state != null, "%s has no state for %s", this, key);
        return state;
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
        return updateField(blockMaterial, () -> WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getMaterial(this));
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
    public String toString() {
        return getId();
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
