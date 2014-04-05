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

package com.sk89q.worldedit;

import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates new {@link EditSession}s. To get an instance of this factory,
 * use {@link WorldEdit#getEditSessionFactory()}.
 * </p>
 * It is no longer possible to replace the instance of this in WorldEdit
 * with a custom one. Use {@link EditSessionEvent} to override
 * the creation of {@link EditSession}s.
 */
public class EditSessionFactory {

    @Deprecated
    public EditSession getEditSession(LocalWorld world, int maxBlocks) {
        return getEditSession((World) world, maxBlocks);
    }

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     */
    public EditSession getEditSession(World world, int maxBlocks) {
        throw new IllegalArgumentException("This class is being removed");
    }

    @Deprecated
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        return getEditSession((World) world, maxBlocks, player);
    }

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param player the player that the {@link EditSession} is for
     */
    public EditSession getEditSession(World world, int maxBlocks, LocalPlayer player) {
        throw new IllegalArgumentException("This class is being removed");
    }

    @Deprecated
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
        return getEditSession((World) world, maxBlocks, blockBag);
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param blockBag an optional {@link BlockBag} to use, otherwise null
     */
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag) {
        throw new IllegalArgumentException("This class is being removed");
    }

    @Deprecated
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        return getEditSession((World) world, maxBlocks, blockBag, player);
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param blockBag an optional {@link BlockBag} to use, otherwise null
     * @param player the player that the {@link EditSession} is for
     */
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        throw new IllegalArgumentException("This class is being removed");
    }

    /**
     * Internal factory for {@link EditSession}s.
     */
    static final class EditSessionFactoryImpl extends EditSessionFactory {

        private final EventBus eventBus;

        /**
         * Create a new factory.
         *
         * @param eventBus the event bus
         */
        public EditSessionFactoryImpl(EventBus eventBus) {
            checkNotNull(eventBus);
            this.eventBus = eventBus;
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks) {
            return new EditSession(eventBus, world, maxBlocks, null, new EditSessionEvent(world, null, maxBlocks, null));
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks, LocalPlayer player) {
            return new EditSession(eventBus, world, maxBlocks, null, new EditSessionEvent(world, player, maxBlocks, null));
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag) {
            return new EditSession(eventBus, world, maxBlocks, blockBag, new EditSessionEvent(world, null, maxBlocks, null));
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
            return new EditSession(eventBus, world, maxBlocks, blockBag, new EditSessionEvent(world, player, maxBlocks, null));
        }

    }
}
