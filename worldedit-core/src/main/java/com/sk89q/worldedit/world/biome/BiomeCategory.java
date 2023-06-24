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

package com.sk89q.worldedit.world.biome;

import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A category of biomes.
 */
public class BiomeCategory extends Category<BiomeType> implements Keyed {

    public static final NamespacedRegistry<BiomeCategory> REGISTRY = new NamespacedRegistry<>("biome tag", true);

    public BiomeCategory(final String id) {
        super(id);
    }

    public BiomeCategory(final String id, final Supplier<Set<BiomeType>> contentSupplier) {
        super(id, contentSupplier);
    }

    @Override
    protected Set<BiomeType> load() {
        return Set.of();
    }

}
