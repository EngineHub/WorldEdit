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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;

import java.nio.file.Path;
import java.util.PriorityQueue;
import javax.annotation.Nullable;

/**
 * An abstract implementation of {@link World}.
 */
public abstract class AbstractWorld implements World {

    private final PriorityQueue<QueuedEffect> effectQueue = new PriorityQueue<>();
    private int taskId = -1;

    @Override
    public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
        return false;
    }

    @Override
    public final <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 pt, B block) throws WorldEditException {
        return setBlock(pt, block, SideEffectSet.defaults());
    }

    @Override
    public Path getStoragePath() {
        return null;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getMaxY() {
        return 255;
    }

    @Override
    public Mask createLiquidMask() {
        return new BlockTypeMask(this, BlockTypes.LAVA, BlockTypes.WATER);
    }

    @Override
    public void dropItem(Vector3 pt, BaseItemStack item, int times) {
        for (int i = 0; i < times; ++i) {
            dropItem(pt, item);
        }
    }

    @Override
    public void checkLoadedChunk(BlockVector3 pt) {
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
    }

    @Override
    public void fixLighting(Iterable<BlockVector2> chunks) {
    }

    @Override
    public boolean playEffect(Vector3 position, int type, int data) {
        return false;
    }

    @Override
    public boolean queueBlockBreakEffect(Platform server, BlockVector3 position, BlockType blockType, double priority) {
        if (taskId == -1) {
            taskId = server.schedule(0, 1, () -> {
                int max = Math.max(1, Math.min(30, effectQueue.size() / 3));
                for (int i = 0; i < max; ++i) {
                    if (effectQueue.isEmpty()) {
                        return;
                    }

                    effectQueue.poll().play();
                }
            });
        }

        if (taskId == -1) {
            return false;
        }

        effectQueue.offer(new QueuedEffect(position.toVector3(), blockType, priority));

        return true;
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return BlockVector3.at(-30000000, getMinY(), -30000000);
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return BlockVector3.at(30000000, getMaxY(), 30000000);
    }

    @Override
    public @Nullable Operation commit() {
        return null;
    }

    @Override
    public WeatherType getWeather() {
        return WeatherTypes.CLEAR;
    }

    @Override
    public long getRemainingWeatherDuration() {
        return 0;
    }

    @Override
    public void setWeather(WeatherType weatherType) {
    }

    @Override
    public void setWeather(WeatherType weatherType, long duration) {
    }

    private class QueuedEffect implements Comparable<QueuedEffect> {
        private final Vector3 position;
        private final BlockType blockType;
        private final double priority;

        private QueuedEffect(Vector3 position, BlockType blockType, double priority) {
            this.position = position;
            this.blockType = blockType;
            this.priority = priority;
        }

        @SuppressWarnings("deprecation")
        public void play() {
            playEffect(position, 2001, blockType.getLegacyId());
        }

        @Override
        public int compareTo(@Nullable QueuedEffect other) {
            return Double.compare(priority, other != null ? other.priority : 0);
        }
    }

}
