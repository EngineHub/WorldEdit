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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.sponge.internal.SpongeTransmogrifier;
import com.sk89q.worldedit.world.fluid.FluidType;
import com.sk89q.worldedit.world.registry.FluidRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.api.state.StateProperty;

import java.util.Map;
import java.util.TreeMap;

final class SpongeFluidRegistry implements FluidRegistry {
    @Override
    public Map<String, ? extends Property<?>> getProperties(FluidType fluidType) {
        var fluid = BuiltInRegistries.FLUID.stream()
            .filter(candidate -> BuiltInRegistries.FLUID.getKey(candidate).toString().equals(fluidType.id()))
            .findFirst().orElseThrow();
        Map<String, Property<?>> properties = new TreeMap<>();
        for (var property : fluid.defaultFluidState().getProperties()) {
            Property<?> worldEditProperty = SpongeTransmogrifier.transmogToWorldEditProperty(
                (StateProperty<?>) property);
            properties.put(worldEditProperty.name(), worldEditProperty);
        }
        return properties;
    }
}
