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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4;

import com.sk89q.worldedit.bukkit.adapter.Refraction;

/**
 * Dedicated class to map all names that we use.
 *
 * <p>
 * Overloads are split into multiple fields, as they <em>CAN</em> have different obfuscated names.
 * </p>
 */
public final class StaticRefraction {
    public static final String GET_CHUNK_FUTURE_MAIN_THREAD = Refraction.pickName(
        "getChunkFutureMainThread", "c"
    );
    public static final String MAIN_THREAD_PROCESSOR = Refraction.pickName(
        "mainThreadProcessor", "g"
    );
    public static final String NEXT_TICK_TIME = Refraction.pickName("nextTickTime", "e");
    public static final String GET_BLOCK_STATE = Refraction.pickName("getBlockState", "a_");
    /**
     * {@code addFreshEntityWithPassengers(Entity entity)}.
     */
    public static final String ADD_FRESH_ENTITY_WITH_PASSENGERS_ENTITY = Refraction.pickName(
        "addFreshEntityWithPassengers", "a_"
    );
    /**
     * {@code addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason)}.
     */
    public static final String ADD_FRESH_ENTITY_WITH_PASSENGERS_ENTITY_SPAWN_REASON =
        Refraction.pickName("addFreshEntityWithPassengers", "a_");
    /**
     * {@code addFreshEntity(Entity entity)}.
     */
    public static final String ADD_FRESH_ENTITY = Refraction.pickName("addFreshEntity", "b");
    /**
     * {@code addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason)}.
     */
    public static final String ADD_FRESH_ENTITY_SPAWN_REASON = Refraction.pickName(
        "addFreshEntity", "b"
    );
    /**
     * {@code getBlockEntity(BlockPos blockPos)}.
     */
    public static final String GET_BLOCK_ENTITY = Refraction.pickName("getBlockEntity", "c_");
    /**
     * {@code setBlock(BlockPos blockPos, BlockState blockState, int flags)}.
     */
    public static final String SET_BLOCK = Refraction.pickName("setBlock", "a");
    /**
     * {@code setBlock(BlockPos blockPos, BlockState blockState, int flags, int maxUpdateDepth)}.
     */
    public static final String SET_BLOCK_MAX_UPDATE = Refraction.pickName("setBlock", "a");
    public static final String REMOVE_BLOCK = Refraction.pickName("removeBlock", "a");
    /**
     * {@code destroyBlock(BlockPos blockPos, boolean drop)}.
     */
    public static final String DESTROY_BLOCK = Refraction.pickName("destroyBlock", "b");
    /**
     * {@code destroyBlock(BlockPos blockPos, boolean drop, Entity breakingEntity)}.
     */
    public static final String DESTROY_BLOCK_BREAKING_ENTITY = Refraction.pickName(
        "destroyBlock", "a"
    );
    /**
     * {@code destroyBlock(BlockPos blockPos, boolean drop, Entity breakingEntity, int maxUpdateDepth)}.
     */
    public static final String DESTROY_BLOCK_BREAKING_ENTITY_MAX_UPDATE = Refraction.pickName(
        "destroyBlock", "a"
    );
}
