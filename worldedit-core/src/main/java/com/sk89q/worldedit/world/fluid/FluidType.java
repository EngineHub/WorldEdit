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

package com.sk89q.worldedit.world.fluid;


import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Minecraft now has a 'fluid' system. This is a stub class to represent what it may be in the future.
 */
public final class FluidType implements Keyed {

    public static final NamespacedRegistry<FluidType> REGISTRY = new NamespacedRegistry<>("fluid type", "fluid_type", "minecraft");

    private final String id;
    @SuppressWarnings("this-escape")
    private final LazyReference<Map<String, ? extends Property<?>>> properties =
        LazyReference.from(() -> computeProperties(this));
    @SuppressWarnings("this-escape")
    private final LazyReference<FluidState> defaultState =
        LazyReference.from(() -> getInternalStateList().getFirst());
    @SuppressWarnings("this-escape")
    private final LazyReference<FluidTypeStateList> internalStateList =
        LazyReference.from(() -> FluidTypeStateList.createFor(this));

    public FluidType(String id) {
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        this.id = id;
    }

    private static Map<String, ? extends Property<?>> computeProperties(FluidType self) {
        Map<String, ? extends Property<?>> propertiesMap = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.GAME_HOOKS).getRegistries().getFluidRegistry().getProperties(self);
        String[] propertyNames = propertiesMap.keySet().toArray(new String[0]);
        Arrays.sort(propertyNames);
        Object[] properties = new Object[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            properties[i] = propertiesMap.get(propertyNames[i]);
        }
        return Object2ObjectMaps.unmodifiable(new Object2ObjectArrayMap<>(propertyNames, properties));
    }

    @Override
    public String id() {
        return id;
    }

    FluidTypeStateList getInternalStateList() {
        return internalStateList.getValue();
    }

    /**
     * Gets the default state of this fluid type.
     *
     * @return the default state
     */
    public FluidState getDefaultState() {
        return defaultState.getValue();
    }

    public Map<String, ? extends Property<?>> getPropertyMap() {
        return properties.getValue();
    }

    public List<? extends Property<?>> getProperties() {
        return List.copyOf(getPropertyMap().values());
    }

    public <V> Property<V> getProperty(String name) {
        @SuppressWarnings("unchecked")
        Property<V> property = (Property<V>) getPropertyMap().get(name);
        if (property == null) {
            throw new IllegalArgumentException(this + " has no property named " + name);
        }
        return property;
    }

    public List<FluidState> getAllStates() {
        return getInternalStateList();
    }

    public FluidState getState(Map<Property<?>, Object> values) {
        return getInternalStateList().get(getInternalStateList().calculateIndex(values));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FluidType fluidType && id.equals(fluidType.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
