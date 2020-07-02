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

package com.sk89q.worldedit.world;

import com.google.auto.value.AutoValue;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.regions.Region;

import java.util.OptionalLong;

/**
 * Regeneration options for {@link World#regenerate(Region, EditSession, RegenOptions)}.
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
    public interface Builder {

        /**
         * Sets the seed to regenerate with. Defaults to {@link OptionalLong#empty()}.
         *
         * <p>
         * Use {@link OptionalLong#empty()} to use the world's current seed.
         * </p>
         *
         * @param seed the seed to regenerate with
         * @return this builder
         */
        Builder seed(OptionalLong seed);

        /**
         * Turn on or off applying the biomes from the regenerated chunk. Defaults to {@code false}.
         *
         * @param regenBiomes {@code true} to apply biomes
         * @return this builder
         */
        Builder regenBiomes(boolean regenBiomes);

        /**
         * Build the options object.
         *
         * @return the options object
         */
        RegenOptions build();

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


    /**
     * Whether biomes should be regenerated.
     */
    public abstract boolean isRegenBiomes();

}
