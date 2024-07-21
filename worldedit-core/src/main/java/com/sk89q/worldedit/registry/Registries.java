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

package com.sk89q.worldedit.registry;

import com.sk89q.worldedit.world.biome.BiomeCategory;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.fluid.FluidCategory;
import com.sk89q.worldedit.world.fluid.FluidType;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.weather.WeatherType;

public class Registries {
    public static final Registry<BlockType> BLOCK_TYPE = addRegistry(BlockType.REGISTRY);
    public static final Registry<BlockCategory> BLOCK_CATEGORY = addRegistry(BlockCategory.REGISTRY);
    public static final Registry<ItemType> ITEM_TYPE = addRegistry(ItemType.REGISTRY);
    public static final Registry<ItemCategory> ITEM_CATEGORY = addRegistry(ItemCategory.REGISTRY);
    public static final Registry<GameMode> GAME_MODE = addRegistry(GameMode.REGISTRY);
    public static final Registry<WeatherType> WEATHER_TYPE = addRegistry(WeatherType.REGISTRY);
    public static final Registry<BiomeType> BIOME_TYPE = addRegistry(BiomeType.REGISTRY);
    public static final Registry<BiomeCategory> BIOME_CATEGORY = addRegistry(BiomeCategory.REGISTRY);
    public static final Registry<EntityType> ENTITY_TYPE = addRegistry(EntityType.REGISTRY);
    public static final Registry<FluidType> FLUID_TYPE = addRegistry(FluidType.REGISTRY);
    public static final Registry<FluidCategory> FLUID_CATEGORY = addRegistry(FluidCategory.REGISTRY);
    public static final Registry<ConfiguredFeatureType> CONFIGURED_FEATURE_TYPE = addRegistry(ConfiguredFeatureType.REGISTRY);
    public static final Registry<StructureType> STRUCTURE_TYPE = addRegistry(StructureType.REGISTRY);

    private static <T extends Keyed> Registry<T> addRegistry(Registry<T> registry) {
        Registry.REGISTRY.register(registry.id(), registry);
        return registry;
    }
}
