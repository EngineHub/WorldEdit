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

package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

/**
 * @deprecated Use {@link com.sk89q.worldedit.function.mask.Mask}
 */
@Deprecated
public interface Mask {

    /**
     * Called one time before each edit session.
     *
     * @param session
     * @param player
     * @param target target of the brush, null if not a brush mask
     */
    void prepare(LocalSession session, LocalPlayer player, Vector target);

    /**
     * Given a block position, this method returns true if the block at
     * that position matches the filter. Block information is not provided
     * as getting a BaseBlock has unneeded overhead in most block querying
     * situations (enumerating a chest's contents is a waste, for example).
     *
     * @param editSession
     * @param pos
     * @return
     */
    boolean matches(EditSession editSession, Vector pos);
}
