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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.registry.Keyed;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public final class RegistryConverter<V extends Keyed> implements ArgumentConverter<V> {

    @SuppressWarnings("unchecked")
    public static void register(CommandManager commandManager) {
        ImmutableList.of(
            BlockType.class,
            BlockCategory.class,
            ItemType.class,
            ItemCategory.class,
            BiomeType.class,
            EntityType.class,
            FluidType.class,
            FluidCategory.class,
            GameMode.class,
            WeatherType.class
        ).stream()
            .map(c -> (Class<Keyed>) c)
            .forEach(registryType ->
                commandManager.registerConverter(Key.of(registryType), from(registryType))
            );
    }

    @SuppressWarnings("unchecked")
    private static <V extends Keyed> RegistryConverter<V> from(Class<Keyed> registryType) {
        try {
            Field registryField = registryType.getDeclaredField("REGISTRY");
            Registry<V> registry = (Registry<V>) registryField.get(null);
            return new RegistryConverter<>(registry);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Not a registry-backed type: " + registryType.getName());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Registry field inaccessible on " + registryType.getName());
        }
    }

    private final Registry<V> registry;
    private final TextComponent choices;

    private RegistryConverter(Registry<V> registry) {
        this.registry = registry;
        this.choices = TextComponent.of("any " + registry.getName());
    }

    @Override
    public Component describeAcceptableArguments() {
        return this.choices;
    }

    @Override
    public ConversionResult<V> convert(String argument, InjectedValueAccess injectedValueAccess) {
        V result = registry.get(argument);
        return result == null
                ? FailedConversion.from(new IllegalArgumentException(
                    "Not a valid " + registry.getName() + ": " + argument))
                : SuccessfulConversion.fromSingle(result);
    }

    @Override
    public List<String> getSuggestions(String input) {
        return SuggestionHelper.getRegistrySuggestions(registry, input).collect(Collectors.toList());
    }
}
