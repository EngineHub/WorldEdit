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

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.util.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;

/**
 * An implementation of {@link Registries} that converts legacy numeric IDs and
 * a contains a built-in block and item database.
 */
public class BundledRegistries implements Registries {

    private static final BundledRegistries INSTANCE = new BundledRegistries();

    static URL loadRegistry(String name) throws IOException {
        ResourceLoader resourceLoader = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION)
            .getResourceLoader();
        URL url = resourceLoader.getResource(BundledRegistries.class, name + ".json");
        if (url == null) {
            throw new IOException("Could not find " + name + ".json");
        }
        return url;
    }

    @SuppressWarnings("removal")
    private final BundledBlockRegistry blockRegistry = new BundledBlockRegistry();
    @SuppressWarnings("removal")
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
