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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable class that represents the state a block can be in.
 */
@SuppressWarnings("unchecked")
public class BlockState implements BlockStateHolder<BlockState> {

    static {
        BlockStateIdAccess.setBlockStateInternalId(new BlockStateIdAccess.BlockStateInternalId() {
            @Override
            public int getInternalId(BlockState blockState) {
                return blockState.internalId;
            }

            @Override
            public void setInternalId(BlockState blockState, int internalId) {
                blockState.internalId = internalId;
            }
        });
    }

    private final BlockType blockType;
    private final Map<Property<?>, Object> values;

    private final BaseBlock emptyBaseBlock;

    // Neighbouring state table.
    private Table<Property<?>, Object, BlockState> states;

    /**
     * The internal ID of the block state.
     */
    private int internalId = BlockStateIdAccess.invalidId();

    BlockState(BlockType blockType) {
        this.blockType = blockType;
        this.values = new LinkedHashMap<>();
        this.emptyBaseBlock = new BaseBlock(this);
    }

    static Map<Map<Property<?>, Object>, BlockState> generateStateMap(BlockType blockType) {
        ImmutableMap.Builder<Map<Property<?>, Object>, BlockState> stateMapBuilder = ImmutableMap.builder();
        List<? extends Property<?>> properties = blockType.getProperties();

        if (!properties.isEmpty()) {
            List<List<Object>> separatedValues = Lists.newArrayList();
            for (Property<?> prop : properties) {
                List<Object> vals = Lists.newArrayList();
                vals.addAll(prop.getValues());
                separatedValues.add(vals);
            }
            List<List<Object>> valueLists = Lists.cartesianProduct(separatedValues);
            for (List<Object> valueList : valueLists) {
                Map<Property<?>, Object> valueMap = Maps.newTreeMap(Comparator.comparing(Property::getName));
                BlockState stateMaker = new BlockState(blockType);
                for (int i = 0; i < valueList.size(); i++) {
                    Property<?> property = properties.get(i);
                    Object value = valueList.get(i);
                    valueMap.put(property, value);
                    stateMaker.setState(property, value);
                }
                stateMapBuilder.put(ImmutableMap.copyOf(valueMap), stateMaker);
            }
        }

        ImmutableMap<Map<Property<?>, Object>, BlockState> stateMap = stateMapBuilder.build();

        if (stateMap.isEmpty()) {
            // No properties.
            stateMap = ImmutableMap.of(ImmutableMap.of(), new BlockState(blockType));
        }

        for (BlockState state : stateMap.values()) {
            state.populate(stateMap);
        }

        // Sometimes loading can take a while. This is the perfect spot to let MC know we're working.
        Watchdog watchdog = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS)
            .getWatchdog();
        if (watchdog != null) {
            watchdog.tick();
        }

        return stateMap;
    }

    private void populate(Map<Map<Property<?>, Object>, BlockState> stateMap) {
        final ImmutableTable.Builder<Property<?>, Object, BlockState> states = ImmutableTable.builder();

        for (final Map.Entry<Property<?>, Object> entry : this.values.entrySet()) {
            final Property<Object> property = (Property<Object>) entry.getKey();

            for (Object value : property.getValues()) {
                if (value != entry.getValue()) {
                    BlockState modifiedState = stateMap.get(this.withValue(property, value));
                    if (modifiedState != null) {
                        states.put(property, value, modifiedState);
                    } else {
                        System.out.println(stateMap);
                        WorldEdit.logger.warn("Found a null state at " + this.withValue(property, value));
                    }
                }
            }
        }

        this.states = states.build();
    }

    private <V> Map<Property<?>, Object> withValue(final Property<V> property, final V value) {
        final ImmutableMap.Builder<Property<?>, Object> values = ImmutableMap.builder();
        for (Map.Entry<Property<?>, Object> entry : this.values.entrySet()) {
            if (entry.getKey().equals(property)) {
                values.put(entry.getKey(), value);
            } else {
                values.put(entry);
            }
        }
        return values.build();
    }

    @Override
    public BlockType getBlockType() {
        return this.blockType;
    }

    @Override
    public <V> BlockState with(final Property<V> property, final V value) {
        BlockState result = states.get(property, value);
        return result == null ? this : result;
    }

    @Override
    public <V> V getState(final Property<V> property) {
        return (V) this.values.get(property);
    }

    @Override
    public Map<Property<?>, Object> getStates() {
        return Collections.unmodifiableMap(this.values);
    }

    @Override
    public boolean equalsFuzzy(BlockStateHolder<?> o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            // Added a reference equality check for speediness
            return true;
        }
        if (!getBlockType().equals(o.getBlockType())) {
            return false;
        }

        Set<Property<?>> differingProperties = new HashSet<>();
        for (Object state : o.getStates().keySet()) {
            if (getState((Property<?>) state) == null) {
                differingProperties.add((Property<?>) state);
            }
        }
        for (Property<?> property : getStates().keySet()) {
            if (o.getState(property) == null) {
                differingProperties.add(property);
            }
        }

        for (Property<?> property : getStates().keySet()) {
            if (differingProperties.contains(property)) {
                continue;
            }
            if (!Objects.equals(getState(property), o.getState(property))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockState toImmutableState() {
        return this;
    }

    @Override
    public BaseBlock toBaseBlock() {
        return this.emptyBaseBlock;
    }

    @Override
    public BaseBlock toBaseBlock(LazyReference<CompoundBinaryTag> compoundTag) {
        if (compoundTag == null) {
            return toBaseBlock();
        }
        return new BaseBlock(this, compoundTag);
    }

    /**
     * Internal method used for creating the initial BlockState.
     *
     * @param property The state
     * @param value The value
     * @return The blockstate, for chaining
     */
    BlockState setState(final Property<?> property, final Object value) {
        this.values.put(property, value);
        return this;
    }

    @Override
    public String toString() {
        return getAsString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockState)) {
            return false;
        }

        return equalsFuzzy((BlockState) obj);
    }

    private Integer hashCodeCache = null;

    @Override
    public int hashCode() {
        if (hashCodeCache == null) {
            hashCodeCache = Objects.hash(blockType, values);
        }
        return hashCodeCache;
    }
}
