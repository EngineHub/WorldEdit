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

package com.sk89q.worldedit.world;

import com.google.auto.value.AutoValue;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;

import java.util.OptionalLong;
import javax.annotation.Nullable;

/**
 * Regeneration options for {@link World#regenerate(Region, Extent, RegenOptions)}.
 */
@AutoValue
public abstract class RegenOptions {

    /**
     * Creates a new options builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new AutoValue_RegenOptions.Builder().seed(OptionalLong.empty()).regenBiomes(false);
    }

    @AutoValue.Builder
    public abstract static class Builder {

        /**
         * Sets the seed to regenerate with. Defaults to {@code null}.
         *
         * <p>
         * Use {@code null} to use the world's current seed.
         * </p>
         *
         * @param seed the seed to regenerate with
         * @return this builder
         */
        public final Builder seed(@Nullable Long seed) {
            return seed(seed == null ? OptionalLong.empty() : OptionalLong.of(seed));
        }

        // AV doesn't like us using @Nullable Long for some reason
        abstract Builder seed(OptionalLong seed);

        /**
         * Turn on or off applying the biomes from the regenerated chunk. Defaults to {@code false}.
         *
         * @param regenBiomes {@code true} to apply biomes
         * @return this builder
         */
        public abstract Builder regenBiomes(boolean regenBiomes);

        /**
         * Build the options object.
         *
         * @return the options object
         */
        public abstract RegenOptions build();

    }

    RegenOptions() {
    }

    /**
     * The seed to regenerate with.
     *
     * <p>
     * {@link OptionalLong#empty()} if the world's original seed should be used.
     * </p>
     */
    public abstract OptionalLong getSeed();

    abstract boolean isRegenBiomes();

    /**
     * Whether biomes should be regenerated.
     */
    public final boolean shouldRegenBiomes() {
        return isRegenBiomes();
    }

}
