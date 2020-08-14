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

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.biome.BiomeType;

import javax.annotation.Nullable;

/**
 * A biome registry that knows nothing.
 */
public class NullBiomeRegistry implements BiomeRegistry {

    /**
     * Create a new instance.
     */
    public NullBiomeRegistry() {
    }

    @Override
    public Component getRichName(BiomeType biomeType) {
        return TranslatableComponent.of(
            TranslationManager.makeTranslationKey("biome", biomeType.getId())
        );
    }

    @Deprecated
    @Nullable
    @Override
    public BiomeData getData(BiomeType biome) {
        return null;
    }

}
