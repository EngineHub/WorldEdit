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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.biome.BiomeData;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import org.spongepowered.api.world.biome.BiomeType;

import javax.annotation.Nullable;

/**
 * Provides access to biome data in Sponge.
 */
class SpongeBiomeRegistry implements BiomeRegistry {

    @Override
    public Component getRichName(com.sk89q.worldedit.world.biome.BiomeType biomeType) {
        return TranslatableComponent.of(
            TranslationManager.makeTranslationKey("biome", biomeType.getId())
        );
    }

    @Deprecated
    @Nullable
    @Override
    public BiomeData getData(com.sk89q.worldedit.world.biome.BiomeType biome) {
        return new SpongeBiomeData(SpongeAdapter.adapt(biome));
    }

    @Deprecated
    private static class SpongeBiomeData implements BiomeData {
        private final BiomeType biome;

        /**
         * Create a new instance.
         *
         * @param biome the base biome
         */
        private SpongeBiomeData(BiomeType biome) {
            this.biome = biome;
        }

        @SuppressWarnings("deprecation")
        @Override
        public String getName() {
            return biome.getName();
        }
    }

}
