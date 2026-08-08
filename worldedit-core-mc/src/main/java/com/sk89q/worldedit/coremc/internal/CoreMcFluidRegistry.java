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

package com.sk89q.worldedit.coremc.internal;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.fluid.FluidType;
import com.sk89q.worldedit.world.registry.FluidRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;
import java.util.TreeMap;

final class CoreMcFluidRegistry implements FluidRegistry {
    private final CoreMcPlatform platform;

    CoreMcFluidRegistry(CoreMcPlatform platform) {
        this.platform = platform;
    }

    @Override
    public Map<String, ? extends Property<?>> getProperties(FluidType fluidType) {
        var fluid = BuiltInRegistries.FLUID.stream()
            .filter(candidate -> BuiltInRegistries.FLUID.getKey(candidate).toString().equals(fluidType.id()))
            .findFirst().orElseThrow();
        Map<String, Property<?>> properties = new TreeMap<>();
        for (var property : fluid.defaultFluidState().getProperties()) {
            Property<?> worldEditProperty = platform.getTransmogrifier().transmogToWorldEditProperty(property);
            properties.put(worldEditProperty.name(), worldEditProperty);
        }
        return properties;
    }
}
