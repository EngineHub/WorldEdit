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

import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

/**
 * A mask that matches another mask offset.
 */
public class UnderOverlayMask implements Mask {

    private final int yMod;
    private Mask mask;

    /**
     * Create a new mask for a list of block types.
     * 
     * @param ids a list of types
     * @param overlay true to offset Y by 1, otherwise false to offset by -1
     */
    @Deprecated
    public UnderOverlayMask(Set<Integer> ids, boolean overlay) {
        this(new BlockTypeMask(ids), overlay); 
    }
    
    /**
     * Create a new mask for another mask.
     * 
     * @param mask the mask
     * @param overlay true to offset Y by 1, otherwise false to offset by -1
     */
    public UnderOverlayMask(Mask mask, boolean overlay) {
        this.yMod = overlay ? -1 : 1;
        this.mask = mask;
    }

    /**
     * Get the Y offset.
     * 
     * @return the Y offset
     */
    public int getYMod() {
        return yMod;
    }

    /**
     * Get the underlying mask.
     * 
     * @return the underlying mask
     */
    public Mask getMask() {
        return mask;
    }

    @Deprecated
    public void addAll(Set<Integer> ids) {
        if (mask instanceof BlockTypeMask) {
            BlockTypeMask blockTypeMask = (BlockTypeMask) mask;
            for (Integer id : ids) {
                blockTypeMask.add(id);
            }
        } else if (mask instanceof ExistingBlockMask) {
            mask = new BlockTypeMask(ids);
        }
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
        mask.prepare(session, player, target);
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return mask.matches(editSession, pos.add(0, yMod, 0));
    }

    @Override
    public String toString() {
        return String.format("UnderOverlayMask(yMod=%d, mask=%s)", yMod, mask);
    }

}
