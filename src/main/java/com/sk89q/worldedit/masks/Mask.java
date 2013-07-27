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

package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

/**
 * Masks are used to filter which kind of blocks are replaced.
 */
public interface Mask {

    /**
     * Called one time before each edit session.
     *
     * @param session the session
     * @param player the player
     * @param target target of the brush, null if not a brush mask
     */
    public void prepare(LocalSession session, LocalPlayer player, Vector target);

    /**
     * Given a block position, this method returns true if the block at
     * that position matches the filter.
     * 
     * @param editSession the edit session
     * @param position the position of the block
     * @return true if the block at the given position matches the mask
     */
    public boolean matches(EditSession editSession, Vector position);
}
