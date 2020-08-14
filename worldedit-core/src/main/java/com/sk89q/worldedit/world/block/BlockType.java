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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

public class BlockType implements Keyed {

    public static final NamespacedRegistry<BlockType> REGISTRY = new NamespacedRegistry<>("block type");

    private final String id;
    private final Function<BlockState, BlockState> values;
    private final LazyReference<BlockState> defaultState
        = LazyReference.from(this::computeDefaultState);
    private final LazyReference<FuzzyBlockState> emptyFuzzy
        = LazyReference.from(() -> new FuzzyBlockState(this));
    private final LazyReference<Map<String, ? extends Property<?>>> properties
        = LazyReference.from(() -> ImmutableMap.copyOf(WorldEdit.getInstance().getPlatformManager()
        .queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getProperties(this)));
    private final LazyReference<BlockMaterial> blockMaterial
        = LazyReference.from(() -> WorldEdit.getInstance().getPlatformManager()
        .queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getMaterial(this));
    private final LazyReference<Map<Map<Property<?>, Object>, BlockState>> blockStatesMap
        = LazyReference.from(() -> BlockState.generateStateMap(this));

    @Deprecated
    private final LazyReference<String> name = LazyReference.from(() -> WorldEdit.getInstance().getPlatformManager()
        .queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getName(this));
    private final LazyReference<Integer> legacyId = LazyReference.from(() -> computeLegacy(0));
    private final LazyReference<Integer> legacyData = LazyReference.from(() -> computeLegacy(1));

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

    private BlockState computeDefaultState() {
        BlockState defaultState = Iterables.getFirst(getBlockStatesMap().values(), null);
        if (values != null) {
            defaultState = values.apply(defaultState);
        }
        return defaultState;
    }

    private Map<Map<Property<?>, Object>, BlockState> getBlockStatesMap() {
        return blockStatesMap.getValue();
    }

    /**
     * Gets the ID of this block.
     *
     * @return The id
     */
    @Override
    public String getId() {
        return this.id;
    }

    public Component getRichName() {
        return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS)
            .getRegistries().getBlockRegistry().getRichName(this);
    }

    /**
     * Gets the name of this block, or the ID if the name cannot be found.
     *
     * @return The name, or ID
     * @deprecated The name is now translatable, use {@link #getRichName()}.
     */
    @Deprecated
    public String getName() {
        String name = this.name.getValue();
        if (name == null || name.isEmpty()) {
            return getId();
        }
        return name;
    }

    /**
     * Gets the properties of this BlockType in a {@code key->property} mapping.
     *
     * @return The properties map
     */
    public Map<String, ? extends Property<?>> getPropertyMap() {
        return properties.getValue();
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
        return defaultState.getValue();
    }

    public FuzzyBlockState getFuzzyMatcher() {
        return emptyFuzzy.getValue();
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
        return blockMaterial.getValue();
    }

    /**
     * Gets the legacy ID. Needed for legacy reasons.
     *
     * <p>
     * DO NOT USE THIS.
     * </p>
     *
     * @return legacy id or 0, if unknown
     */
    @Deprecated
    public int getLegacyId() {
        return legacyId.getValue();
    }

    /**
     * Gets the legacy data. Needed for legacy reasons.
     *
     * <p>
     * DO NOT USE THIS.
     * </p>
     *
     * @return legacy data or 0, if unknown
     */
    @Deprecated
    public int getLegacyData() {
        return legacyData.getValue();
    }

    private int computeLegacy(int index) {
        int[] legacy = LegacyMapper.getInstance().getLegacyFromBlock(this.getDefaultState());
        return legacy != null ? legacy[index] : 0;
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
