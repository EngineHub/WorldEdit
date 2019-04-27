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

package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.fluid.FluidCategory;
import com.sk89q.worldedit.world.fluid.FluidType;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.weather.WeatherType;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.stream.Collectors;

public class RegistryConverter<V> implements ArgumentConverter<V> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(BlockType.class),
                new RegistryConverter<>(BlockType.class, BlockType.REGISTRY));
        commandManager.registerConverter(Key.of(BlockCategory.class),
                new RegistryConverter<>(BlockCategory.class, BlockCategory.REGISTRY));
        commandManager.registerConverter(Key.of(ItemType.class),
                new RegistryConverter<>(ItemType.class, ItemType.REGISTRY));
        commandManager.registerConverter(Key.of(ItemCategory.class),
                new RegistryConverter<>(ItemCategory.class, ItemCategory.REGISTRY));
        commandManager.registerConverter(Key.of(BiomeType.class),
                new RegistryConverter<>(BiomeType.class, BiomeType.REGISTRY));
        commandManager.registerConverter(Key.of(EntityType.class),
                new RegistryConverter<>(EntityType.class, EntityType.REGISTRY));
        commandManager.registerConverter(Key.of(FluidType.class),
                new RegistryConverter<>(FluidType.class, FluidType.REGISTRY));
        commandManager.registerConverter(Key.of(FluidCategory.class),
                new RegistryConverter<>(FluidCategory.class, FluidCategory.REGISTRY));
        commandManager.registerConverter(Key.of(GameMode.class),
                new RegistryConverter<>(GameMode.class, GameMode.REGISTRY));
        commandManager.registerConverter(Key.of(WeatherType.class),
                new RegistryConverter<>(WeatherType.class, WeatherType.REGISTRY));
    }

    private final Registry<V> registry;
    private final TextComponent choices;

    public RegistryConverter(Class<V> clazz, Registry<V> registry) {
        this.registry = registry;
        this.choices = TextComponent.of("any " + clazz.getSimpleName());
    }

    @Override
    public Component describeAcceptableArguments() {
        return this.choices;
    }

    @Override
    public ConversionResult<V> convert(String argument, InjectedValueAccess injectedValueAccess) {
        V result = registry.get(argument);
        return result == null
                ? FailedConversion.from(new IllegalArgumentException("Not a valid choice: " + argument))
                : SuccessfulConversion.fromSingle(result);
    }

    @Override
    public List<String> getSuggestions(String input) {
        return registry.keySet().stream()
                .filter(string -> string.startsWith(input))
                .collect(Collectors.toList());
    }
}
