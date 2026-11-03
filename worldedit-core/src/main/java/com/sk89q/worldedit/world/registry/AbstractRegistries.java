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

package com.sk89q.worldedit.world.registry;

/**
 * An implementation of {@link Registries} that knows nothing about the world.
 */
public abstract class AbstractRegistries implements Registries {

    private final BlockRegistry blockRegistry = new NullBlockRegistry();
    private final ItemRegistry itemRegistry = new NullItemRegistry();
    private final EntityRegistry entityRegistry = new NullEntityRegistry();
    private final BiomeRegistry biomeRegistry = new NullBiomeRegistry();
    private final BlockCategoryRegistry blockCategoryRegistry = new NullBlockCategoryRegistry();
    private final ItemCategoryRegistry itemCategoryRegistry = new NullItemCategoryRegistry();

    @Override
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    @Override
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    @Override
    public EntityRegistry getEntityRegistry() {
        return entityRegistry;
    }

    @Override
    public BiomeRegistry getBiomeRegistry() {
        return biomeRegistry;
    }

    @Override
    public BlockCategoryRegistry getBlockCategoryRegistry() {
        return blockCategoryRegistry;
    }

    @Override
    public ItemCategoryRegistry getItemCategoryRegistry() {
        return itemCategoryRegistry;
    }
}
