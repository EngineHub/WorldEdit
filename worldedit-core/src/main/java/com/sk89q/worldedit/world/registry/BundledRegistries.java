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
 * An implementation of {@link Registries} that converts legacy numeric IDs and
 * a contains a built-in block and item database.
 */
public class BundledRegistries implements Registries {

    private static final BundledRegistries INSTANCE = new BundledRegistries();
    private final BundledBlockRegistry blockRegistry = new BundledBlockRegistry();
    private final BundledItemRegistry itemRegistry = new BundledItemRegistry();
    private final NullEntityRegistry entityRegistry = new NullEntityRegistry();
    private final NullBiomeRegistry biomeRegistry = new NullBiomeRegistry();
    private final NullBlockCategoryRegistry blockCategoryRegistry = new NullBlockCategoryRegistry();
    private final NullItemCategoryRegistry itemCategoryRegistry = new NullItemCategoryRegistry();

    /**
     * Create a new instance.
     */
    protected BundledRegistries() {
    }

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

    /**
     * Get a singleton instance.
     *
     * @return an instance
     */
    public static BundledRegistries getInstance() {
        return INSTANCE;
    }

}
