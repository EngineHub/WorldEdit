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

package com.sk89q.worldedit;

import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates new {@link EditSession}s. To get an instance of this factory,
 * use {@link WorldEdit#getEditSessionFactory()}.
 *
 * <p>It is no longer possible to replace the instance of this in WorldEdit
 * with a custom one. Use {@link EditSessionEvent} to override
 * the creation of {@link EditSession}s.</p>
 *
 * @deprecated Using the ever-extending factory methods is deprecated. Replace with {@link EditSessionBuilder},
 *     which in most cases will be as simple as calling {@code builder.world(world).build()}.
 */
@Deprecated
public class EditSessionFactory {

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @return an instance
     */
    public EditSession getEditSession(World world, int maxBlocks) {

        //  ============ READ ME ============

        // This method is actually implemented if you call WorldEdit.getEditSessionFactory()
        // as it returns an instance of EditSessionFactoryImpl seen below.

        // Previously, other plugins would create their own EditSessionFactory and extend ours and
        // then use it to return custom EditSessions so the plugin could log block changes, etc.
        // However, that method only allows one plugin to hook into WorldEdit at a time,
        // so now we recommend catching the EditSessionEvent and hooking into our
        // new(er) Extent framework.

        throw new RuntimeException("Method needs to be implemented");
    }

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param actor the actor that the {@link EditSession} is for
     * @return an instance
     */
    public EditSession getEditSession(World world, int maxBlocks, Actor actor) {

        //  ============ READ ME ============

        // This method is actually implemented if you call WorldEdit.getEditSessionFactory()
        // as it returns an instance of EditSessionFactoryImpl seen below.

        // Previously, other plugins would create their own EditSessionFactory and extend ours and
        // then use it to return custom EditSessions so the plugin could log block changes, etc.
        // However, that method only allows one plugin to hook into WorldEdit at a time,
        // so now we recommend catching the EditSessionEvent and hooking into our
        // new(er) Extent framework.

        throw new RuntimeException("Method needs to be implemented");
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param blockBag an optional {@link BlockBag} to use, otherwise null
     * @return an instance
     */
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag) {

        //  ============ READ ME ============

        // This method is actually implemented if you call WorldEdit.getEditSessionFactory()
        // as it returns an instance of EditSessionFactoryImpl seen below.

        // Previously, other plugins would create their own EditSessionFactory and extend ours and
        // then use it to return custom EditSessions so the plugin could log block changes, etc.
        // However, that method only allows one plugin to hook into WorldEdit at a time,
        // so now we recommend catching the EditSessionEvent and hooking into our
        // new(er) Extent framework.

        throw new RuntimeException("Method needs to be implemented");
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param blockBag an optional {@link BlockBag} to use, otherwise null
     * @param actor the actor that the {@link EditSession} is for
     * @return an instance
     */
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, Actor actor) {

        //  ============ READ ME ============

        // This method is actually implemented if you call WorldEdit.getEditSessionFactory()
        // as it returns an instance of EditSessionFactoryImpl seen below.

        // Previously, other plugins would create their own EditSessionFactory and extend ours and
        // then use it to return custom EditSessions so the plugin could log block changes, etc.
        // However, that method only allows one plugin to hook into WorldEdit at a time,
        // so now we recommend catching the EditSessionEvent and hooking into our
        // new(er) Extent framework.

        throw new RuntimeException("Method needs to be implemented");
    }

    /**
     * Internal factory for {@link EditSession}s.
     */
    static final class EditSessionFactoryImpl extends EditSessionFactory {

        /**
         * Create a new factory.
         */
        EditSessionFactoryImpl() {
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks) {
            return getEditSession(world, maxBlocks, null, null);
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks, Actor actor) {
            return getEditSession(world, maxBlocks, null, actor);
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag) {
            return getEditSession(world, maxBlocks, blockBag, null);
        }

        @Override
        public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, Actor actor) {
            return WorldEdit.getInstance().newEditSessionBuilder()
                .world(world)
                .maxBlocks(maxBlocks)
                .blockBag(blockBag)
                .actor(actor)
                .build();
        }

    }
}
