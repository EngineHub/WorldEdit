// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

import com.sk89q.worldedit.bags.BlockBag;

public class EditSessionFactory {

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world
     * @param maxBlocks
     * @return an edit session
     */
    public EditSession getEditSession(LocalWorld world, int maxBlocks) {
        return new EditSession(world, maxBlocks);
    }

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world
     * @param maxBlocks
     * @param player
     * @return an edit session
     */
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        return this.getEditSession(world, maxBlocks);
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world
     * @param maxBlocks
     * @param blockBag
     * @return an edit session
     */
    @SuppressWarnings("deprecation")
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
        return new EditSession(world, maxBlocks, blockBag);
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world
     * @param maxBlocks
     * @param blockBag
     * @param player
     * @return an edit session
     */
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        return this.getEditSession(world, maxBlocks, blockBag);
    }

}
