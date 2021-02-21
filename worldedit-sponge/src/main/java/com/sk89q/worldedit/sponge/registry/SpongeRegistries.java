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

package com.sk89q.worldedit.sponge.registry;

import com.sk89q.worldedit.world.registry.BiomeRegistry;
import com.sk89q.worldedit.world.registry.BlockCategoryRegistry;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.BundledRegistries;
import com.sk89q.worldedit.world.registry.ItemCategoryRegistry;
import com.sk89q.worldedit.world.registry.ItemRegistry;

/**
 * Registry data for the Sponge platform.
 */
public class SpongeRegistries extends BundledRegistries {

    private static final SpongeRegistries INSTANCE = new SpongeRegistries();
    private final BiomeRegistry biomeRegistry = new SpongeBiomeRegistry();
    private final BlockRegistry blockRegistry = new SpongeBlockRegistry();
    private final BlockCategoryRegistry blockCategoryRegistry = new SpongeBlockCategoryRegistry();
    private final ItemCategoryRegistry itemCategoryRegistry = new SpongeItemCategoryRegistry();
    private final ItemRegistry itemRegistry = new SpongeItemRegistry();

    @Override
    public BiomeRegistry getBiomeRegistry() {
        return this.biomeRegistry;
    }

    @Override
    public BlockRegistry getBlockRegistry() {
        return this.blockRegistry;
    }

    @Override
    public BlockCategoryRegistry getBlockCategoryRegistry() {
        return this.blockCategoryRegistry;
    }

    @Override
    public ItemCategoryRegistry getItemCategoryRegistry() {
        return this.itemCategoryRegistry;
    }

    @Override
    public ItemRegistry getItemRegistry() {
        return this.itemRegistry;
    }

    /**
     * Get a static instance.
     *
     * @return an instance
     */
    public static SpongeRegistries getInstance() {
        return INSTANCE;
    }

}
