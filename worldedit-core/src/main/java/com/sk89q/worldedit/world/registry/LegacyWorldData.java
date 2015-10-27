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

package com.sk89q.worldedit.world.registry;

/**
 * An implementation of {@link WorldData} that uses legacy numeric IDs and
 * a built-in block database.
 */
public class LegacyWorldData implements WorldData {

    private static final LegacyWorldData INSTANCE = new LegacyWorldData();
    private final LegacyBlockRegistry blockRegistry = new LegacyBlockRegistry();
    private final NullItemRegistry itemRegistry = new NullItemRegistry();
    private final NullEntityRegistry entityRegistry = new NullEntityRegistry();
    private final NullBiomeRegistry biomeRegistry = new NullBiomeRegistry();

    /**
     * Create a new instance.
     */
    protected LegacyWorldData() {
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

    /**
     * Get a singleton instance.
     *
     * @return an instance
     */
    public static LegacyWorldData getInstance() {
        return INSTANCE;
    }

}
